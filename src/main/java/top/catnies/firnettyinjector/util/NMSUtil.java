package top.catnies.firnettyinjector.util;

import io.netty.channel.ChannelFuture;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConnectionListener;

import java.lang.reflect.Field;
import java.util.List;

public class NMSUtil {

    /** 获取服务器实例 **/
    public static MinecraftServer getServerInstance() {
        return MinecraftServer.getServer();
    }


    /** 获取服务器链接的 ChannelFuture 列表 **/
    public static List<ChannelFuture> getServerChannelFutureList() throws NoSuchFieldException, IllegalAccessException {
        ServerConnectionListener serverConnection = getServerInstance().getConnection();
        Field field = ReflectiveUtil.getFieldByType(serverConnection, List.class);
        return ReflectiveUtil.getFieldValue(serverConnection, field);
    }


    public static List<ChannelFuture> getServerChannelFutureListByName() throws NoSuchFieldException, IllegalAccessException {
        ServerConnectionListener serverConnection = getServerInstance().getConnection();
        Field channelFutures = serverConnection.getClass().getField("f");
        return (List<ChannelFuture>) channelFutures.get(serverConnection);
    }

}
