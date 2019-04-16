package com.vtradex.ehub.jtt.jtt809;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vtradex.ehub.jtt.entity.JT809Constants;
import com.vtradex.ehub.jtt.entity.LoginRequest;
import com.vtradex.ehub.jtt.entity.Message;
import com.vtradex.ehub.jtt.entity.Vehicle;
import com.vtradex.ehub.jtt.util.ByteBufPool;
import com.vtradex.ehub.jtt.util.DecimalConversion;
import com.vtradex.ehub.jtt.util.PropertiesUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Jtt809Handler extends SimpleChannelInboundHandler<Message>  {
    
	private static Logger LOGGER=LoggerFactory.getLogger(Jtt809Handler.class);

	private Jtt809NettyServer server;
	
	private LoginStatusEnum loginStatus;
	/**
	 * 当前是否为主链路
	 */
	private boolean isMajor=false;
	
    public Jtt809Handler(Jtt809NettyServer server) {
    	this.server=server;
    	isMajor=true;
    }
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
		//如果是主链路且登陆状态不是成功，则除非是登陆请求，否则都不放行
		if(isMajor&&server.getLoginStatus()!=LoginStatusEnum.runnable) {
			if(JT809Constants.UP_CONNECT_REQ!=msg.getMsgId()) {
				LOGGER.warn("还没有登陆，不能处理非登陆请求,msgGesscenterId：{}",msg.getMsgGesscenterId());
			}
		}
		switch (msg.getMsgId()) {
		case JT809Constants.UP_CONNECT_REQ:
			handlerLogin(ctx,msg);
			break;
		case JT809Constants.UP_EXG_MSG:
			handlerBiz(ctx,msg);
			break;
		case JT809Constants.UP_LINKETEST_REQ:
			handlerHold(ctx,msg);
			break;
		case JT809Constants.DOWN_CONNECT_RSP:
			handlerDownConnect(ctx,msg);
			break;
		default:
			LOGGER.info("未处理的消息类型：{}",DecimalConversion.intToHex(msg.getMsgId()));
			break;
		}
		
		
	}
	
	/**
	 * 处理从链路连接请求得到的响应
	 * @param ctx
	 * @param msg
	 */
	private void handlerDownConnect(ChannelHandlerContext ctx, Message msg) {
		int result=msg.getMsgBody().readByte();
		if(JT809Constants.DOWN_CONNECT_RSP_SUCCESS!=result) {
			LOGGER.info(msg.getMsgGesscenterId()+"从链路连接请求响应失败："+result);
		}else {
		    LOGGER.info(msg.getMsgGesscenterId()+"从链路连接请求响应成功");
		}
	}

	/**
	 * 主链路连接保持消息处理，这种消息数据体为空，直接发送响应即可
	 * @param ctx
	 * @param msg
	 */
	private void handlerHold(ChannelHandlerContext ctx, Message msg) {
		Message message=new Message(JT809Constants.UP_LINKTEST_RSP,msg.getMsgGesscenterId());
	    Jtt809Util.sendServerMessage(ctx, message);
	}

	/**
	 * 处理业务
	 * @param ctx
	 * @param msg
	 */
	private void handlerBiz(ChannelHandlerContext ctx, Message msg) {
		//如果未登陆，则直接拒绝
		ByteBuf msgBody=msg.getMsgBody();
		//构建车辆信息交换对象
		Optional<Vehicle> op=Vehicle.builderVehicle(msgBody,msg.getMsgGesscenterId());
		if(op.isPresent()) {
			String orgKey = PropertiesUtil.getString("jtt809"+msg.getMsgGesscenterId()+"orgKey");
			int locationNum=op.get().handlerBiz(orgKey);
			Optional<Message> repOp=LocationUploadCountUtil.addLocationNum(msg.getMsgGesscenterId(), locationNum,100);
			if(repOp.isPresent()) {
				//需要发送响应，判断从链路是否接通，接通就发送
				if(isMajor&&server.getClientMap().containsKey(msg.getMsgGesscenterId())) {
					Jtt809NettyClient client=server.getClientMap().get(msg.getMsgGesscenterId());
					if(client.getChannel().isActive()) {
						repOp.get().send(client.getChannel());
					}else {
						LOGGER.info("{}从链路未建立，不发送定位统计响应报文",msg.getMsgGesscenterId());
					}
				}else {
					LOGGER.info("{}从链路未建立，不发送定位统计响应报文",msg.getMsgGesscenterId());
				}
			}
		}
		msg=null;
	}


	/**
	 * 处理登陆请求
	 * @param ctx
	 * @param msg
	 */
	private void handlerLogin(ChannelHandlerContext ctx, Message msg) {
		//判断登陆
		 if (server.getLoginStatus()==LoginStatusEnum.init) {
			 server.setLoginStatus(LoginStatusEnum.waiting);
	            ctx.executor().schedule(new ConnectionTerminator(ctx,msg.getMsgGesscenterId()), 5, TimeUnit.SECONDS);
	    }
		//转换登陆请求报文
		LoginRequest requeset=Jtt809Util.convertLoginRequest(msg);
		//校验参数，获取响应码
		int responseCode=validateLogin(msg,requeset);
	    LOGGER.info("接入码：{},用户：{} 登陆，登陆时间：{},输入的密码是：{},响应码：{}",msg.getMsgGesscenterId(),
	    		requeset.getUserId(),new Date(),requeset.getPassword(),responseCode);
	    //生成一个四位随机数响应码，用于建立从链路校验
	    int verifyCode = (int)Math.random()*1000;
	    //构建登陆响应报文
	    Message loginResponse=builderLoginResponse(responseCode,msg.getMsgGesscenterId(),verifyCode);
	    //响应报文发送
	    loginResponse.send(ctx);
	    //根据响应码设置连接状态
        if(responseCode!=JT809Constants.UP_CONNECT_RSP_SUCCESS) {
        	//断开该连接
        	ctx.close();
        }else {
        	LOGGER.info("接入码：{},用户：{} 登陆成功，登陆时间：{}",msg.getMsgGesscenterId(),
    	    		requeset.getUserId(),new Date());
        	server.setLoginStatus(LoginStatusEnum.runnable);
        }
        /**构建从链路*/
        buildClient(msg.getMsgGesscenterId(),requeset.getDownLinkIp(),requeset.getDownLinkPort(),verifyCode);
	}

	/**
	 * 构建从链路客户端
	 */
	private void buildClient(int msgGesscenterId,String ip,int port,int verifyCode) {
		//如果当前处理的是主链路登陆请求,则拿到对应msgGesscenterId的从链路对象
		boolean reconnection=true;
		if(isMajor) {
        	Jtt809NettyClient client=server.getClientMap().get(msgGesscenterId);
        	if(client!=null) {
        		//如果存在该从链路，判断ip和端口是否一致，一致不用重连
        		if(ip.equals(client.getIp())&&port==client.getPort()) {
    				if(client.getChannel().isActive()) {
    					reconnection=false;
            		}
    			}else {
    				//ip不一致，关闭该从链路
    				client.getChannel().close();
    			}
        	}
        }
		if(reconnection) {
			synchronized (msgGesscenterId+ip+port) {
				Jtt809NettyClient client=new Jtt809NettyClient(ip,port);
				try {
					client.init();
				} catch (InterruptedException e) {
					LOGGER.error(String.format("%s 从链路建立失败: %s:%s",msgGesscenterId,ip,port),e);
					return;
				}
				//循环5秒或者直到从链路建立成功，否则超时
		        for(int i=0;i<5;i++) {
		        	if(!client.getChannel().isActive()) {
		        		try {
							TimeUnit.SECONDS.sleep(1);
						} catch (InterruptedException e) {
							LOGGER.warn("从链路建立时发生间断异常",e);
						}
		        	}
		        }
		        if(!client.getChannel().isActive()) {
		        	LOGGER.error(String.format("%s 从链路建立连接超时，超时时间5秒: %s:%s",msgGesscenterId,ip,port));
		        }else {
		        	server.getClientMap().put(msgGesscenterId, client);
		        	LOGGER.error(String.format("%s 从链路建立连接成功: %s:%s",msgGesscenterId,ip,port));
		        	//构建成功后发送连接请求
		        	Message message=new Message(JT809Constants.DOWN_CONNECT_REQ,msgGesscenterId);
		        	ByteBuf body=Unpooled.buffer(4);
		            body.writeInt(verifyCode);
		            message.setMsgBody(body);
		            message.send(client.getChannel());
		        }
			}
			
		}
	}

	/**
	 * 构建登陆响应报文
	 * @param responseCode
	 * @return
	 */
	private Message builderLoginResponse(int responseCode,int msgGesscenterId,int verifyCode) {
		Message response=new Message(JT809Constants.UP_CONNECT_RSP,msgGesscenterId);
        ByteBuf body=ByteBufPool.BYTE_BUF_POOL.buffer(5);
        body.writeByte(responseCode);
        body.writeInt(verifyCode);
        response.setMsgBody(body);
        return response;
        
		
	}

	private int validateLogin(Message msg,LoginRequest request) {
		//判断接入码是否存在
		String msgGesscenterId=PropertiesUtil.getString("jtt809."+msg.getMsgGesscenterId()+".msgGesscenterId");
		if(msgGesscenterId==null) {
			return JT809Constants.UP_CONNECT_RSP_ERROR_02;
		}
		//判断用户是否存在
		int userId=PropertiesUtil.getInteger("jtt809."+msg.getMsgGesscenterId()+".userId");
		if(userId!=request.getUserId()) {
			return JT809Constants.UP_CONNECT_RSP_ERROR_03;
		}
		String password=PropertiesUtil.getString("jtt809."+msg.getMsgGesscenterId()+".password");
		//判断密码是否正确
		if(!password.equals(request.getPassword())) {
			return JT809Constants.UP_CONNECT_RSP_ERROR_04;
		}
		//判断ip地址是否正确
        LOGGER.info("传入的ip地址和端口为：{}，{},尚未校验",request.getDownLinkIp(),request.getDownLinkPort());
		return JT809Constants.UP_CONNECT_RSP_SUCCESS;
	}


	private class ConnectionTerminator implements Runnable{
        ChannelHandlerContext ctx;
        int msgGesscenterId;
        public ConnectionTerminator(ChannelHandlerContext ctx,int msgGesscenterId) {
            this.ctx = ctx;
            this.msgGesscenterId=msgGesscenterId;
        }
        public void run() {
            // TODO Auto-generated method stub
            if (server.getLoginStatus()!=LoginStatusEnum.runnable) {
            	LOGGER.info("接入码为：{} 的下级平台，在连接5秒内没有登陆成功，连接断开",msgGesscenterId);
                ctx.close();
            }  
        }
    } 

}
