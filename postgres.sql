CREATE DATABASE insurance_db;

\c insurance_db;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    nick text NOT NULL,
    login text UNIQUE NOT NULL,
    password text NOT NULL,
    insert_time timestamp DEFAULT current_timestamp
);

CREATE TABLE vehicles (
    id SERIAL PRIMARY KEY,
    login text NOT NULL REFERENCES users(login),
    brand text NOT NULL,
    model text NOT NULL,
    insert_time timestamp NOT NULL DEFAULT current_timestamp
);

CREATE TABLE insurance_offers (
    id SERIAL PRIMARY KEY,
    vehicle_id bigint NOT NULL REFERENCES vehicles(id),
    insurer text NOT NULL,
    price float NOT NULL,
    insert_time timestamp NOT NULL DEFAULT current_timestamp
);

INSERT INTO users (nick, login, password) VALUES
    ('Anna Nowak', 'anna_nowak', 'haslo123'),
    ('Piotr Kowalski', 'piotr_kowalski', 'tajne456'),
    ('Ewa Jankowska', 'ewa_jankowska', 'sekretne789'),
    ('Marta Kowalczyk', 'marta_kowalczyk', 'mypass123'),
    ('Krzysztof Nowicki', 'krzysztof_nowicki', 'secure456'),
    ('Magdalena Szymańska', 'magdalena_szymanska', 'pass1234');


INSERT INTO vehicles (login, brand, model) VALUES
    ('anna_nowak', 'Fiat', '500'),
    ('anna_nowak', 'Opel', 'Astra'),
    ('piotr_kowalski', 'Volkswagen', 'Golf'),
    ('ewa_jankowska', 'Renault', 'Clio'),
    ('ewa_jankowska', 'Skoda', 'Octavia'),
    ('marta_kowalczyk', 'Renault', 'Megane'),
    ('krzysztof_nowicki', 'Toyota', 'Rav4'),
    ('krzysztof_nowicki', 'Peugeot', '308'),
    ('magdalena_szymanska', 'Ford', 'Focus'),
    ('magdalena_szymanska', 'BMW', 'X3');

INSERT INTO insurance_offers (vehicle_id, insurer, price) VALUES
    (1, 'PZU', 400.00),
    (2, 'Warta', 550.00),
    (2, 'Compensa', 600.00),
    (3, 'Aviva', 480.00),
    (3, 'Generali', 700.00),
    (4, 'Link4', 350.00),
    (5, 'Allianz', 600.00),
    (5, 'Axa', 750.00),
    (6, 'Proama', 450.00),
    (7, 'Euroins', 520.00),
    (7, 'Uniqa', 680.00),
    (8, 'HDI', 500.00),
    (9, 'Liberty Mutual', 620.00);



