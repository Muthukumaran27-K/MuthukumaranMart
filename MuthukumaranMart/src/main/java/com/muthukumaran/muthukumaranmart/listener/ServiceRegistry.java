package com.muthukumaran.muthukumaranmart.listener;

import com.muthukumaran.muthukumaranmart.dao.CartDAO;
import com.muthukumaran.muthukumaranmart.dao.OrderDAO;
import com.muthukumaran.muthukumaranmart.dao.ProductDAO;
import com.muthukumaran.muthukumaranmart.dao.ReviewDAO;
import com.muthukumaran.muthukumaranmart.dao.UserDAO;
import com.muthukumaran.muthukumaranmart.dao.impl.JdbcCartDAO;
import com.muthukumaran.muthukumaranmart.dao.impl.JdbcOrderDAO;
import com.muthukumaran.muthukumaranmart.dao.impl.JdbcProductDAO;
import com.muthukumaran.muthukumaranmart.dao.impl.JdbcReviewDAO;
import com.muthukumaran.muthukumaranmart.dao.impl.JdbcUserDAO;
import com.muthukumaran.muthukumaranmart.chat.ChatProvider;
import com.muthukumaran.muthukumaranmart.chat.ChatService;
import com.muthukumaran.muthukumaranmart.chat.GeminiChatProvider;
import com.muthukumaran.muthukumaranmart.chat.MockChatProvider;
import com.muthukumaran.muthukumaranmart.service.CartService;
import com.muthukumaran.muthukumaranmart.service.OrderService;
import com.muthukumaran.muthukumaranmart.service.ProductService;
import com.muthukumaran.muthukumaranmart.service.ReviewService;
import com.muthukumaran.muthukumaranmart.service.UserService;

import javax.sql.DataSource;

/**
 * Small hand-rolled DI container: wires DAO implementations (Factory pattern,
 * Section 12) into services once at application startup and hands the same
 * instances to every servlet via the ServletContext. No framework needed at
 * this project's scale.
 */
public final class ServiceRegistry {

    public static final String ATTR = "muthukumaranmart.services";

    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final ChatService chatService;

    public ServiceRegistry(DataSource dataSource) {
        this(dataSource, loadChatConfig());
    }

    public ServiceRegistry(DataSource dataSource, java.util.Properties chatConfig) {
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        ProductDAO productDAO = new JdbcProductDAO(dataSource);
        CartDAO cartDAO = new JdbcCartDAO(dataSource);
        OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        ReviewDAO reviewDAO = new JdbcReviewDAO(dataSource);

        this.userService = new UserService(userDAO);
        this.productService = new ProductService(productDAO);
        this.cartService = new CartService(cartDAO, productDAO);
        this.orderService = new OrderService(dataSource, orderDAO, productDAO, cartDAO);
        this.reviewService = new ReviewService(reviewDAO, orderDAO);

        // Section 17, Rule 2: implementation selected via config flag
        // ai.chatbot.provider=gemini|mock.
        String providerFlag = chatConfig.getProperty("ai.chatbot.provider", "mock");
        ChatProvider provider = "gemini".equalsIgnoreCase(providerFlag)
                ? new GeminiChatProvider(chatConfig.getProperty("ai.chatbot.apiKey", ""))
                : new MockChatProvider();
        this.chatService = new ChatService(provider);
    }

    private static java.util.Properties loadChatConfig() {
        java.util.Properties props = new java.util.Properties();
        try (var in = ServiceRegistry.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (java.io.IOException ignored) {
            // Defaults (mock provider) apply if config.properties is missing.
        }
        return props;
    }

    public UserService userService() {
        return userService;
    }

    public ProductService productService() {
        return productService;
    }

    public CartService cartService() {
        return cartService;
    }

    public OrderService orderService() {
        return orderService;
    }

    public ReviewService reviewService() {
        return reviewService;
    }

    public ChatService chatService() {
        return chatService;
    }
}
