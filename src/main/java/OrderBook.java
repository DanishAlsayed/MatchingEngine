public interface OrderBook {
    boolean fillAndInsert(Order order);
    boolean cancel(int id);
    boolean amend(Amend amend);
}
