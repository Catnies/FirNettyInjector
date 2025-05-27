package top.catnies.firnettyinjector;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import top.catnies.firnettyinjector.api.NettyInjector;

public final class FirNettyInjector extends JavaPlugin implements Listener {

    public static FirNettyInjector instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);

        // 这是一个简单的示例使用方案, 创建NettyInjector后向其添加Consumer, 最后使用 #inject 完成注入.
        try {
            NettyInjector nettyInjector = new NettyInjector();
            nettyInjector.addHandler( pipeline -> {
                pipeline.addBefore("packet_handler", "test_handler", new PrintPacketHandler());
            });
            nettyInjector.inject();
        } catch (Exception e) {
            throw new RuntimeException("注入Handler出现异常!");
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


    // 这个事件主要是用于Debug, 可以看到在玩家加入服务器后, 内部的Initializer最后是被正确的移除了.
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer serverPlayer = craftPlayer.getHandle();
        Connection connection = serverPlayer.connection.connection;
        System.out.println("进服时玩家的Pipeline: " +  String.join(", ", connection.channel.pipeline().names()));
    }

}
