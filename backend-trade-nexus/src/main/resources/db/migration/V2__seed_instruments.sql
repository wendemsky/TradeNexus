INSERT INTO instrument (instrument_id, ticker, external_id_type, external_id, category_id, description, max_quantity, min_quantity, coupon_rate, maturity_date) VALUES
('GOOGL', 'GOOGL', 'TICKER', 'GOOGL', 'STOCK', 'Alphabet Inc. Class A',       1000,  1,   NULL,   NULL),
('TSLA',  'TSLA',  'TICKER', 'TSLA',  'STOCK', 'Tesla Inc.',                  1000,  1,   NULL,   NULL),
('JPM',   'JPM',   'TICKER', 'JPM',   'STOCK', 'JPMorgan Chase & Co.',         1000,  1,   NULL,   NULL),
('BRK-B', 'BRK-B', 'TICKER', 'BRK-B', 'STOCK', 'Berkshire Hathaway Class B',  100,   1,   NULL,   NULL),
('AAPL',  'AAPL',  'TICKER', 'AAPL',  'STOCK', 'Apple Inc.',                  1000,  1,   NULL,   NULL),
('MSFT',  'MSFT',  'TICKER', 'MSFT',  'STOCK', 'Microsoft Corp.',             1000,  1,   NULL,   NULL),
('SPY',   'SPY',   'TICKER', 'SPY',   'ETF',   'SPDR S&P 500 ETF Trust',      1000,  1,   NULL,   NULL),
('US2Y',  'DGS2',  'FRED',   'DGS2',  'GOVT',  'US Treasury 2-Year Note',     10000, 100, 0.0425, '2027-05-15'),
('US5Y',  'DGS5',  'FRED',   'DGS5',  'GOVT',  'US Treasury 5-Year Note',     10000, 100, 0.0400, '2029-05-15'),
('US10Y', 'DGS10', 'FRED',   'DGS10', 'GOVT',  'US Treasury 10-Year Note',    10000, 100, 0.0425, '2034-05-15'),
('US20Y', 'DGS20', 'FRED',   'DGS20', 'GOVT',  'US Treasury 20-Year Bond',    10000, 100, 0.0438, '2044-05-15'),
('US30Y', 'DGS30', 'FRED',   'DGS30', 'GOVT',  'US Treasury 30-Year Bond',    10000, 100, 0.0450, '2054-05-15');
