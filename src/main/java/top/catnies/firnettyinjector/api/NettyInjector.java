package top.catnies.firnettyinjector.api;

import io.netty.channel.*;
import top.catnies.firnettyinjector.util.NMSUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

public class NettyInjector {

    // 一个Set, 维护需要注入的所有操作, 当客户端链接创建完成, 并且默认的handler初始化完成后, 将会获取客户端的Pipeline, 进行操作.
    private final LinkedHashSet<Consumer<ChannelPipeline>> channelInitializerSet = new LinkedHashSet<>();


    public void inject() throws NoSuchFieldException, IllegalAccessException {
        List<ChannelFuture> channelFutures = NMSUtil.getServerChannelFutureList(); // 获取服务端的 ChannelFuture, 这是服务端监听完端口后返回的future, 每个future代表一个端口.
        for (ChannelFuture future : channelFutures) {
            System.out.println(future);
            Channel serverChannel = future.channel();  // 获取服务端的主channel.


            /** 第一个方案 **/
            // 在服务端的主Channel的最前面添加一个处理器, 先让包经过它.
            serverChannel.pipeline().addFirst("FirNettyInjector", new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                    // 这里的msg一定是ClientChannel, 代表每个客户端的链接通道.
                    Channel clientChannel = (Channel) msg;

                    // 因为这是最前面的handler, 所以需要先让后面正常的handler执行完成, 让 Vanilla, PacketEvent 等通道先进行注入.
                    System.out.println("未加载原始玩家Pipeline: " +  String.join(", ", clientChannel.pipeline().names()));
                    super.channelRead(ctx, msg);

                    // 当其他handler已就绪时, 再在客户端的Pipeline中, 添加一个Initializer, 虽然Pipeline已经完成了初始化, 但是Netty的机制是在接收到数据包时, 如果发现了有Initializer还存在, 会再次触发Init并移除自身, 这样也可以确保编辑生效.
                    System.out.println("未注入前玩家的Pipeline: " +  String.join(", ", clientChannel.pipeline().names()));
                    clientChannel.pipeline().addFirst("FirNettyInjector_InitHandler", new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // 我们尝试应用之前注册的所有Consumer.
                            System.out.println("准备注入前玩家Pipeline: " + String.join(", ", clientChannel.pipeline().names()));
                            for (Consumer<ChannelPipeline> pipelineConsumer : channelInitializerSet) pipelineConsumer.accept(ch.pipeline());
                            System.out.println("注入完成的Pipeline: " + String.join(", ", clientChannel.pipeline().names()));
                        }
                    });
                }
            });
            // 只是提前返回, 不让下面的代码报错.
            if (1 == 1) return;


            /** 第二个方案 **/
            // 在服务端的主Channel的最前面添加一个处理器, 先让包经过它.
            serverChannel.pipeline().addFirst("FirNettyInjector", new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                    // 这里的msg一定是ClientChannel, 代表每个客户端的链接通道.
                    System.out.println("未注入前玩家的Pipeline: " +  String.join(", ", ((Channel) msg).pipeline().names()));
                    Channel clientChannel = (Channel) msg;

                    // 这里发生了变化, 现在我们可以往客户端的Pipeline中添加 ChannelInboundHandlerAdapter, 然后用它监听 Active 事件, 这样当玩家进服, 链接活动时才会触发, 而此时Vanilla的handler是一定完成了注册的.
                    clientChannel.pipeline().addFirst("FirNettyInjector_InitHandler", new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 我们尝试应用之前注册的所有Consumer.
                            System.out.println("准备注入前玩家的Pipeline: " + String.join(", ", clientChannel.pipeline().names()));
                            for (Consumer<ChannelPipeline> pipelineConsumer : channelInitializerSet) pipelineConsumer.accept(ctx.pipeline());

                            // 这里需要注意, 这个监听 Active 的 Handler 本质上只是一个触发器, 当我们真正的操作完成后, 这个Handler应该从客户端的Pipeline中删除.
                            ctx.pipeline().remove(this);
                            System.out.println("注入完成的Pipeline: " + String.join(", ", clientChannel.pipeline().names()));

                            // 最后记得要将这个消息继续传递下去.
                            super.channelActive(ctx);
                        }
                    });

                    // 记得要将消息传递下去, 让 Vanilla 初始化handler.
                    super.channelRead(ctx, msg);
                }
            });
            // 只是提前返回, 不让下面的代码报错.
            if (1 == 1) return;

        }

    }

    public void addHandler(Consumer<ChannelPipeline> consumer) {
        channelInitializerSet.add(consumer);
    }

    public void removeHandler(Consumer<ChannelPipeline> consumer) {
        channelInitializerSet.remove(consumer);
    }

}
