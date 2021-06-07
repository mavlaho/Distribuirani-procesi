public interface Sensor extends MsgHandler {
    void localPredicateTrue(VectorClock vc);
}
