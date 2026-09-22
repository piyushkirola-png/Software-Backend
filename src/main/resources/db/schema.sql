CREATE DATABASE IF NOT EXISTS softwareuniverse CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE softwareuniverse;

CREATE TABLE
    IF NOT EXISTS users (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(150) NOT NULL,
        email VARCHAR(150) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        orders role VARCHAR(20) NOT NULL DEFAULT 'USER',
        phone VARCHAR(20),
        avatar_url VARCHAR(500),
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        gender VARCHAR(20) NULL,
        current_address VARCHAR(500) NULL,
        city VARCHAR(100) NULL,
        state VARCHAR(100) NULL,
        country VARCHAR(100) NULL,
        pincode VARCHAR(10) NULL,
        created_at DATETIME,
        updated_at DATETIME,
        INDEX idx_users_email (email),
        INDEX idx_users_role (role)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS revoked_tokens (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        token VARCHAR(512) NOT NULL UNIQUE,
        revoked_at DATETIME,
        expires_at DATETIME NOT NULL,
        INDEX idx_revoked_expires (expires_at)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS otp_codes (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        email VARCHAR(150) NOT NULL,
        code VARCHAR(10) NOT NULL,
        expires_at DATETIME NOT NULL,
        used BOOLEAN NOT NULL DEFAULT FALSE,
        purpose VARCHAR(50),
        created_at DATETIME,
        INDEX idx_otp_email (email)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS password_reset_tokens (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        token VARCHAR(255) NOT NULL UNIQUE,
        email VARCHAR(150) NOT NULL,
        expires_at DATETIME NOT NULL,
        used BOOLEAN NOT NULL DEFAULT FALSE,
        created_at DATETIME,
        INDEX idx_prt_email (email)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS addresses (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL,
        full_name VARCHAR(150) NOT NULL,
        phone VARCHAR(20) NOT NULL,
        address_line1 VARCHAR(255) NOT NULL,
        address_line2 VARCHAR(255),
        city VARCHAR(100) NOT NULL,
        state VARCHAR(100) NOT NULL,
        pincode VARCHAR(10) NOT NULL,
        country VARCHAR(100) NOT NULL DEFAULT 'India',
        is_default BOOLEAN NOT NULL DEFAULT FALSE,
        gst_number VARCHAR(20) NULL,
        created_at DATETIME,
        updated_at DATETIME,
        CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        INDEX idx_address_user (user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS categories (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE,
        slug VARCHAR(100) NOT NULL UNIQUE,
        description VARCHAR(500),
        image_url VARCHAR(500),
        icon_url VARCHAR(500),
        display_order INT DEFAULT 0,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at DATETIME,
        updated_at DATETIME,
        INDEX idx_cat_slug (slug),
        INDEX idx_cat_active (is_active)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS products (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        category_id BIGINT NOT NULL,
        title VARCHAR(255) NOT NULL,
        slug VARCHAR(255) NOT NULL UNIQUE,
        description TEXT,
        short_description VARCHAR(500),
        seo_keywords VARCHAR(500),
        mrp DECIMAL(10, 2),
        price DECIMAL(10, 2) NOT NULL,
        thumbnail_url VARCHAR(500),
        download_file_path VARCHAR(500),
        license_type VARCHAR(50),
        activation_type VARCHAR(50),
        has_variants BOOLEAN NOT NULL DEFAULT FALSE,
        stock_quantity INT DEFAULT 0,
        is_featured BOOLEAN NOT NULL DEFAULT FALSE,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        display_order INT DEFAULT 0,
        rating_avg DOUBLE DEFAULT 0,
        rating_count INT DEFAULT 0,
        created_at DATETIME,
        updated_at DATETIME,
        CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id),
        INDEX idx_product_slug (slug),
        INDEX idx_product_category (category_id),
        INDEX idx_product_featured (is_featured, is_active),
        INDEX idx_product_active (is_active)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS product_variants (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        product_id BIGINT NOT NULL,
        variant_name VARCHAR(100) NOT NULL,
        mrp DECIMAL(10, 2),
        price DECIMAL(10, 2) NOT NULL,
        stock_quantity INT DEFAULT 0,
        display_order INT DEFAULT 0,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at DATETIME,
        updated_at DATETIME,
        CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
        INDEX idx_variant_product (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS product_images (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        product_id BIGINT NOT NULL,
        image_url VARCHAR(500) NOT NULL,
        display_order INT DEFAULT 0,
        created_at DATETIME,
        CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
        INDEX idx_image_product (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS carts (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        created_at DATETIME,
        updated_at DATETIME,
        CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS cart_items (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        cart_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        variant_id BIGINT,
        quantity INT NOT NULL DEFAULT 1,
        unit_price DECIMAL(10, 2) NOT NULL,
        added_at DATETIME,
        CONSTRAINT fk_ci_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
        CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES products (id),
        CONSTRAINT fk_ci_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id),
        INDEX idx_ci_cart (cart_id),
        INDEX idx_ci_product (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS orders (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_number VARCHAR(50) NOT NULL UNIQUE,
        user_id BIGINT NOT NULL,
        address_id BIGINT,
        subtotal DECIMAL(10, 2) NOT NULL,
        discount DECIMAL(10, 2) DEFAULT 0,
        coupon_code VARCHAR(50),
        tax DECIMAL(10, 2) DEFAULT 0,
        total DECIMAL(10, 2) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        customer_email VARCHAR(150) NOT NULL,
        customer_phone VARCHAR(20),
        gst_number VARCHAR(20) NULL,
        notes VARCHAR(500) NULL,
        created_at DATETIME,
        updated_at DATETIME,
        CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id),
        CONSTRAINT fk_order_address FOREIGN KEY (address_id) REFERENCES addresses (id),
        INDEX idx_order_number (order_number),
        INDEX idx_order_user (user_id),
        INDEX idx_order_status (status),
        INDEX idx_order_created (created_at)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS order_items (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        variant_id BIGINT,
        product_title VARCHAR(255) NOT NULL,
        variant_name VARCHAR(100),
        quantity INT NOT NULL DEFAULT 1,
        unit_price DECIMAL(10, 2) NOT NULL,
        line_total DECIMAL(10, 2) NOT NULL,
        CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
        CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products (id),
        CONSTRAINT fk_oi_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id),
        INDEX idx_oi_order (order_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS license_keys (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        product_id BIGINT NOT NULL,
        variant_id BIGINT,
        license_key VARCHAR(255) NOT NULL UNIQUE,
        status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
        order_id BIGINT,
        batch_name VARCHAR(100),
        notes VARCHAR(500),
        reserved_at DATETIME,
        sold_at DATETIME,
        uploaded_at DATETIME,
        CONSTRAINT fk_key_product FOREIGN KEY (product_id) REFERENCES products (id),
        CONSTRAINT fk_key_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id),
        CONSTRAINT fk_key_order FOREIGN KEY (order_id) REFERENCES orders (id),
        INDEX idx_key_status (status),
        INDEX idx_key_product (product_id),
        INDEX idx_key_variant (variant_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS coupons (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(50) NOT NULL UNIQUE,
        description VARCHAR(255),
        type VARCHAR(20) NOT NULL,
        value DECIMAL(10, 2) NOT NULL,
        min_order_amount DECIMAL(10, 2) DEFAULT 0,
        max_discount DECIMAL(10, 2),
        usage_limit INT,
        used_count INT DEFAULT 0,
        per_user_limit INT DEFAULT 1,
        starts_at DATETIME,
        expires_at DATETIME,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at DATETIME,
        updated_at DATETIME,
        INDEX idx_coupon_code (code),
        INDEX idx_coupon_active (is_active)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS coupon_usages (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        coupon_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        order_id BIGINT,
        used_at DATETIME,
        CONSTRAINT fk_cu_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE CASCADE,
        CONSTRAINT fk_cu_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        CONSTRAINT fk_cu_order FOREIGN KEY (order_id) REFERENCES orders (id),
        INDEX idx_cu_coupon_user (coupon_id, user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS reviews (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        product_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        order_id BIGINT,
        rating INT NOT NULL,
        title VARCHAR(200),
        comment TEXT,
        is_verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
        is_approved BOOLEAN NOT NULL DEFAULT FALSE,
        created_at DATETIME,
        updated_at DATETIME,
        CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
        CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES orders (id),
        INDEX idx_review_product (product_id),
        INDEX idx_review_approved (is_approved),
        INDEX idx_review_verified (is_verified_purchase)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS invoices (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        invoice_number VARCHAR(50) NOT NULL UNIQUE,
        order_id BIGINT NOT NULL UNIQUE,
        user_id BIGINT NOT NULL,
        subtotal DECIMAL(10, 2) NOT NULL,
        discount DECIMAL(10, 2) DEFAULT 0,
        cgst DECIMAL(10, 2) DEFAULT 0,
        sgst DECIMAL(10, 2) DEFAULT 0,
        igst DECIMAL(10, 2) DEFAULT 0,
        total_tax DECIMAL(10, 2) DEFAULT 0,
        total DECIMAL(10, 2) NOT NULL,
        buyer_name VARCHAR(150),
        buyer_email VARCHAR(150),
        buyer_phone VARCHAR(20),
        buyer_address TEXT,
        buyer_gstin VARCHAR(20),
        buyer_state VARCHAR(100),
        pdf_path VARCHAR(500),
        generated_at DATETIME,
        CONSTRAINT fk_inv_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
        CONSTRAINT fk_inv_user FOREIGN KEY (user_id) REFERENCES users (id),
        INDEX idx_inv_number (invoice_number),
        INDEX idx_inv_user (user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS payments (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        gateway VARCHAR(50) NOT NULL,
        gateway_order_id VARCHAR(255),
        gateway_payment_id VARCHAR(255),
        gateway_signature VARCHAR(500),
        amount DECIMAL(10, 2) NOT NULL,
        currency VARCHAR(10) DEFAULT 'INR',
        payment_link VARCHAR(500) NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
        failure_reason VARCHAR(500),
        raw_response TEXT,
        created_at DATETIME,
        updated_at DATETIME,
        CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
        CONSTRAINT fk_pay_user FOREIGN KEY (user_id) REFERENCES users (id),
        INDEX idx_pay_order (order_id),
        INDEX idx_pay_gateway_payment (gateway_payment_id),
        INDEX idx_pay_status (status)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS newsletter_subscribers (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        email VARCHAR(150) NOT NULL UNIQUE,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        subscribed_at DATETIME,
        INDEX idx_nl_email (email)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
    IF NOT EXISTS webhook_inbound_logs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        gateway VARCHAR(50) NOT NULL,
        event_type VARCHAR(100),
        payload TEXT,
        signature VARCHAR(500),
        verified BOOLEAN NOT NULL DEFAULT FALSE,
        processed BOOLEAN NOT NULL DEFAULT FALSE,
        error_message VARCHAR(500),
        received_at DATETIME,
        INDEX idx_wh_gateway (gateway),
        INDEX idx_wh_received (received_at)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;