# FirNettyInjector
![Supported Versions](https://img.shields.io/badge/Supported%20Versions-1.21.4-green)

## 📌 关于
这是一个试验性的项目, 主要挖掘了如何在 Minecraft 的 Netty 中注入自定义的 Handler, 仅依靠 NMS 实现数据包的监听, 拦截 和 修改等操作. <br />
这个项目是一个简单的雏形, 并且附加了大量注释, 提供给想了解如  [PacketEvents](https://github.com/retrooper/packetevents) 等数据包库是如何实现的, 如何添加一些自定义数据包人们.<br />
这是一个可构建的插件, 它附带一些Debug消息和测试, 将客户端收到的所有数据包都进行了打印, 除此外不会做其他任何事. <br />

  
## 💻 使用方法
这里有一个用 Java 编写的简单示例：
```Java
public final class FirNettyInjector extends JavaPlugin {

    @Override
    public void onEnable() {
        
        // 这是一个简单的示例使用方案, 创建NettyInjector后向其添加Consumer, 最后使用 #inject 完成注入.
        try {
            NettyInjector nettyInjector = new NettyInjector();
            nettyInjector.addHandler( pipeline -> {
                pipeline.addBefore("packet_handler", "test_handler", new PrintPacketHandler());
            });
            nettyInjector.inject();
        } catch (Exception e) {
            throw new RuntimeException("注入Handler时出现异常!");
        }

    }

    @ChannelHandler.Sharable
    static class PrintPacketHandler extends SimpleChannelInboundHandler<Packet> {
        public PrintPacketHandler() { super(false); }
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
            instance.getLogger().info("服务端接收到了数据包: " + msg);
            ctx.fireChannelRead(msg);
        }
    }

}
```