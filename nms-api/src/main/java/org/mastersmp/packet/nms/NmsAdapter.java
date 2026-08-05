package org.mastersmp.packet.nms;

public interface NmsAdapter {

    String bucketId();

    ConnectionBridge connection();

    PlayerBridge players();

    PacketBridge packets();

    ItemBridge items();

    MenuBridge menus();

    WorldBridge worlds();

    ComponentBridge components();

    PacketWrapper wrapper();

    default MetadataBridge metadata() {
        return UnsupportedMetadata.INSTANCE;
    }

    default RandomBridge random() {
        return UnsupportedRandom.INSTANCE;
    }

    final class UnsupportedMetadata implements MetadataBridge {
        private static final UnsupportedMetadata INSTANCE = new UnsupportedMetadata();

        @Override
        public Object accessor(Class<?> owner, String fieldName) {
            throw new UnsupportedOperationException("metadata");
        }

        @Override
        public Object dataValue(Object accessor, Object value) {
            throw new UnsupportedOperationException("metadata");
        }

        @Override
        public Object dataValue(int id, Object value) {
            throw new UnsupportedOperationException("metadata");
        }

        @Override
        public java.util.List<?> textDisplayValues(
                net.kyori.adventure.text.Component text,
                int lineWidth,
                int backgroundColor,
                byte textOpacity,
                boolean seeThrough
        ) {
            throw new UnsupportedOperationException("metadata");
        }

        @Override
        public int nextEntityId() {
            throw new UnsupportedOperationException("metadata");
        }
    }

    final class UnsupportedRandom implements RandomBridge {
        private static final UnsupportedRandom INSTANCE = new UnsupportedRandom();

        @Override
        public Object gameSource() {
            return null;
        }

        @Override
        public <T> Object weightedOf(java.util.Map<T, Integer> weights) {
            return weights;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T weightedPick(Object weightedList) {
            if (weightedList instanceof java.util.Map<?, ?> map) {
                return (T) pickWeighted((java.util.Map<T, Integer>) map);
            }
            return null;
        }
    }
}
