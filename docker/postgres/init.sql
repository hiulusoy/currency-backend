CREATE TABLE IF NOT EXISTS currencies
(
    code
    VARCHAR
(
    3
) PRIMARY KEY,
    name VARCHAR
(
    100
) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
    );

INSERT INTO currencies (code, name)
SELECT code, name
FROM (VALUES ('USD', 'US Dollar'),
             ('EUR', 'Euro'),
             ('GBP', 'British Pound Sterling'),
             ('JPY', 'Japanese Yen'),
             ('CHF', 'Swiss Franc'),
             ('CAD', 'Canadian Dollar'),
             ('AUD', 'Australian Dollar'),
             ('CNY', 'Chinese Yuan'),
             ('HKD', 'Hong Kong Dollar'),
             ('SGD', 'Singapore Dollar'),
             ('NZD', 'New Zealand Dollar'),
             ('INR', 'Indian Rupee'),
             ('BRL', 'Brazilian Real'),
             ('RUB', 'Russian Ruble'),
             ('KRW', 'South Korean Won'),
             ('ZAR', 'South African Rand'),
             ('MXN', 'Mexican Peso'),
             ('TRY', 'Turkish Lira'),
             ('NOK', 'Norwegian Krone'),
             ('SEK', 'Swedish Krona'),
             ('DKK', 'Danish Krone'),
             ('PLN', 'Polish Zloty'),
             ('CZK', 'Czech Koruna'),
             ('HUF', 'Hungarian Forint'),
             ('THB', 'Thai Baht'),
             ('IDR', 'Indonesian Rupiah'),
             ('MYR', 'Malaysian Ringgit'),
             ('PHP', 'Philippine Peso'),
             ('AED', 'United Arab Emirates Dirham'),
             ('SAR', 'Saudi Riyal')) AS new_values(code, name)
WHERE NOT EXISTS (SELECT 1
                  FROM currencies
                  WHERE currencies.code = new_values.code);
