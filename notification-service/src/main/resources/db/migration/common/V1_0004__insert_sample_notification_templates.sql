INSERT INTO notification_schema.notification_template (channel, code, name, subject, body, description, active, created_date)
VALUES ('EMAIL', 'ORDER_CONFIRMATION', 'Order Confirmation Template',
        'Order #[[${orderNumber}]] Confirmed',
        '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
</head>
<body style="margin:0; padding:0; background-color:#f4f4f7; font-family:Arial, Helvetica, sans-serif;">
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f7;">
    <tr>
        <td align="center" style="padding:40px 0;">
            <table role="presentation" width="600" cellpadding="0" cellspacing="0"
                   style="background-color:#ffffff; border-radius:8px; box-shadow:0 2px 4px rgba(0,0,0,0.1);">
                <tr>
                    <td style="background-color:#2d3748; padding:30px 40px; border-radius:8px 8px 0 0;">
                        <h1 style="color:#ffffff; margin:0; font-size:24px;">Order Confirmed</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:40px;">
                        <p style="color:#333333; font-size:16px; line-height:1.6; margin:0 0 20px;">
                            Dear <span th:text="${customerName}">Customer</span>,
                        </p>
                        <p style="color:#333333; font-size:16px; line-height:1.6; margin:0 0 20px;">
                            Your order <strong>#<span th:text="${orderNumber}">0000</span></strong> has been confirmed
                            on <span th:text="${orderDate}">-</span>.
                        </p>
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0"
                               style="background-color:#f7fafc; border-radius:6px; margin:20px 0;">
                            <tr>
                                <td style="padding:20px;">
                                    <p style="color:#718096; font-size:14px; margin:0 0 8px;">Total Amount</p>
                                    <p style="color:#2d3748; font-size:28px; font-weight:bold; margin:0;">
                                        <span th:text="${totalAmount}">$0.00</span>
                                    </p>
                                </td>
                            </tr>
                        </table>
                        <p style="color:#718096; font-size:14px; line-height:1.6; margin:20px 0 0;">
                            If you have any questions about your order, please contact our support team.
                        </p>
                    </td>
                </tr>
                <tr>
                    <td style="background-color:#f7fafc; padding:20px 40px; border-radius:0 0 8px 8px; text-align:center;">
                        <p style="color:#a0aec0; font-size:12px; margin:0;">
                            This is an automated message. Please do not reply directly to this email.
                        </p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>',
        'Email template sent after a successful order placement', TRUE, CURRENT_TIMESTAMP),

       ('EMAIL', 'PASSWORD_RESET', 'Password Reset Template',
        'Password Reset Request',
        '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
</head>
<body style="margin:0; padding:0; background-color:#f4f4f7; font-family:Arial, Helvetica, sans-serif;">
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f7;">
    <tr>
        <td align="center" style="padding:40px 0;">
            <table role="presentation" width="600" cellpadding="0" cellspacing="0"
                   style="background-color:#ffffff; border-radius:8px; box-shadow:0 2px 4px rgba(0,0,0,0.1);">
                <tr>
                    <td style="background-color:#e53e3e; padding:30px 40px; border-radius:8px 8px 0 0;">
                        <h1 style="color:#ffffff; margin:0; font-size:24px;">Password Reset</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:40px;">
                        <p style="color:#333333; font-size:16px; line-height:1.6; margin:0 0 20px;">
                            Dear <span th:text="${customerName}">Customer</span>,
                        </p>
                        <p style="color:#333333; font-size:16px; line-height:1.6; margin:0 0 20px;">
                            We received a request to reset your password. Click the button below to set a new password.
                        </p>
                        <table role="presentation" cellpadding="0" cellspacing="0" style="margin:30px auto;">
                            <tr>
                                <td style="background-color:#e53e3e; border-radius:6px;">
                                    <a th:href="${resetLink}" href="#"
                                       style="display:inline-block; padding:14px 32px; color:#ffffff; text-decoration:none; font-size:16px; font-weight:bold;">
                                        Reset Password
                                    </a>
                                </td>
                            </tr>
                        </table>
                        <p style="color:#718096; font-size:14px; line-height:1.6; margin:20px 0 0;">
                            This link will expire in <span th:text="${expirationHours}">24</span> hours.
                            If you did not request a password reset, you can safely ignore this email.
                        </p>
                    </td>
                </tr>
                <tr>
                    <td style="background-color:#f7fafc; padding:20px 40px; border-radius:0 0 8px 8px; text-align:center;">
                        <p style="color:#a0aec0; font-size:12px; margin:0;">
                            This is an automated message. Please do not reply directly to this email.
                        </p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>',
        'Email template for password reset requests', TRUE, CURRENT_TIMESTAMP),

       ('EMAIL', 'WELCOME', 'Welcome Email Template',
        'Welcome to Our Platform, [[${customerName}]]!',
        '<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
</head>
<body style="margin:0; padding:0; background-color:#f4f4f7; font-family:Arial, Helvetica, sans-serif;">
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f7;">
    <tr>
        <td align="center" style="padding:40px 0;">
            <table role="presentation" width="600" cellpadding="0" cellspacing="0"
                   style="background-color:#ffffff; border-radius:8px; box-shadow:0 2px 4px rgba(0,0,0,0.1);">
                <tr>
                    <td style="background-color:#38a169; padding:30px 40px; border-radius:8px 8px 0 0;">
                        <h1 style="color:#ffffff; margin:0; font-size:24px;">Welcome!</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:40px;">
                        <p style="color:#333333; font-size:16px; line-height:1.6; margin:0 0 20px;">
                            Hi <span th:text="${customerName}">Customer</span>,
                        </p>
                        <p style="color:#333333; font-size:16px; line-height:1.6; margin:0 0 20px;">
                            Welcome to our platform! We are excited to have you on board.
                        </p>
                        <table role="presentation" cellpadding="0" cellspacing="0" style="margin:30px auto;">
                            <tr>
                                <td style="background-color:#38a169; border-radius:6px;">
                                    <a th:href="${dashboardLink}" href="#"
                                       style="display:inline-block; padding:14px 32px; color:#ffffff; text-decoration:none; font-size:16px; font-weight:bold;">
                                        Go to Dashboard
                                    </a>
                                </td>
                            </tr>
                        </table>
                        <p style="color:#718096; font-size:14px; line-height:1.6; margin:20px 0 0;">
                            If you have any questions, feel free to reach out to our support team.
                        </p>
                    </td>
                </tr>
                <tr>
                    <td style="background-color:#f7fafc; padding:20px 40px; border-radius:0 0 8px 8px; text-align:center;">
                        <p style="color:#a0aec0; font-size:12px; margin:0;">
                            This is an automated message. Please do not reply directly to this email.
                        </p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>',
        'Welcome email sent to new users after registration', TRUE, CURRENT_TIMESTAMP),

       ('SMS', 'ORDER_CONFIRMATION', 'Order Confirmation SMS',
        NULL,
        'Your order #[[${orderNumber}]] has been confirmed. Total: [[${totalAmount}]]. Thank you for your purchase!',
        'SMS template sent after a successful order placement', TRUE, CURRENT_TIMESTAMP),

       ('PUSH', 'ORDER_CONFIRMATION', 'Order Confirmation Push',
        NULL,
        'Your order #[[${orderNumber}]] has been confirmed!',
        'Push notification template for order confirmation', TRUE, CURRENT_TIMESTAMP);
