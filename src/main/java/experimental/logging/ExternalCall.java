package experimental.logging;

public class ExternalCall {
    public static final LogVendor logger = new LogVendor();

    static void main() {
        logger.CreateLog("Hello World!", "INFO");
    }
}
