package protocolsupport.protocol.transformer.mcpe.packet.mcpe.both;

import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.List;

import org.spigotmc.SneakyThrow;

import protocolsupport.protocol.ServerBoundPacket;
import protocolsupport.protocol.transformer.mcpe.handler.PELoginListener;
import protocolsupport.protocol.transformer.mcpe.packet.HandleNMSPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.ClientboundPEPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.DualPEPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.PEPacketIDs;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.ServerboundPEPacket;
import protocolsupport.utils.PacketCreator;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListener;
import net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_8_R3.PlayerConnection;

public class PingPacket implements DualPEPacket {

	protected long pingId;

	public PingPacket() {
	}

	@Override
	public int getId() {
		return PEPacketIDs.PING;
	}

	@Override
	public ServerboundPEPacket decode(ByteBuf buf) throws Exception {
		pingId = buf.readLong();
		return this;
	}

	@Override
	public ClientboundPEPacket encode(ByteBuf buf) throws Exception {
		buf.writeLong(pingId);
		return this;
	}

	@Override
	public List<? extends Packet<?>> transfrom() throws Exception {
		return Collections.singletonList(new HandleNMSPacket<PacketListener>() {
			@Override
			public void handle0(PacketListener listener) {
				if (listener instanceof PELoginListener) {
					((PELoginListener) listener).handleKeepALive(PingPacket.this);
				} else if (listener instanceof PlayerConnection) {
					((PlayerConnection) listener).a(getPlayKeepAlive());
				}
			}
		});
	}

	private PacketPlayInKeepAlive getPlayKeepAlive() {
		try {
			PacketCreator creator = new PacketCreator(ServerBoundPacket.PLAY_KEEP_ALIVE.get());
			creator.writeVarInt((int) pingId);
			return (PacketPlayInKeepAlive) creator.create();
		} catch (Throwable e) {
			SneakyThrow.sneaky(e);
		}
		return null;
	}

	public long getKeepAliveId() {
		return pingId;
	}

}
