-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS MOVIMIENTO;
DROP TABLE IF EXISTS INVENTARIO;
DROP TABLE IF EXISTS BODEGA;
DROP TABLE IF EXISTS PRODUCTO;

-- Table: PRODUCTO
CREATE TABLE PRODUCTO (
    ID SERIAL PRIMARY KEY,
    Nombre VARCHAR(255) NOT NULL,
    Descripcion TEXT,
    Precio DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CantidadEnStock INTEGER NOT NULL DEFAULT 0,
    FechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FechaActualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: BODEGA
CREATE TABLE BODEGA (
    ID SERIAL PRIMARY KEY,
    Nombre VARCHAR(255) NOT NULL,
    Ubicacion VARCHAR(255) NOT NULL,
    Capacidad INTEGER NOT NULL DEFAULT 1000,
    FechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: INVENTARIO
CREATE TABLE INVENTARIO (
    ID SERIAL PRIMARY KEY,
    IDProducto INTEGER NOT NULL,
    IDBodega INTEGER NOT NULL,
    Cantidad INTEGER NOT NULL DEFAULT 0,
    FechaActualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IDProducto) REFERENCES PRODUCTO(ID) ON DELETE CASCADE,
    FOREIGN KEY (IDBodega) REFERENCES BODEGA(ID) ON DELETE CASCADE,
    UNIQUE(IDProducto, IDBodega)
);

-- Table: MOVIMIENTO
CREATE TABLE MOVIMIENTO (
    ID SERIAL PRIMARY KEY,
    IDProducto INTEGER NOT NULL,
    IDBodega INTEGER NOT NULL,
    Tipo VARCHAR(50) NOT NULL CHECK (Tipo IN ('ENTRADA', 'SALIDA', 'TRANSFERENCIA', 'AJUSTE')),
    Cantidad INTEGER NOT NULL,
    Fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Comentario TEXT,
    UsuarioResponsable VARCHAR(255),
    FOREIGN KEY (IDProducto) REFERENCES PRODUCTO(ID) ON DELETE CASCADE,
    FOREIGN KEY (IDBodega) REFERENCES BODEGA(ID) ON DELETE CASCADE
);

-- Insert mock data into PRODUCTO
INSERT INTO PRODUCTO (Nombre, Descripcion, Precio, CantidadEnStock)
VALUES
  ('Arroz', 'Arroz blanco de grano largo', 25.50, 100),
  ('Frijoles', 'Frijoles negros premium', 30.00, 200),
  ('Azúcar', 'Azúcar refinada', 20.00, 150);

-- Insert mock data into BODEGA
INSERT INTO BODEGA (Nombre, Ubicacion, Capacidad)
VALUES
  ('Bodega Central', 'Ciudad Principal', 2000),
  ('Bodega Norte', 'Zona Norte', 1500);

-- Insert mock data into INVENTARIO
INSERT INTO INVENTARIO (IDProducto, IDBodega, Cantidad)
VALUES
  (1, 1, 80),
  (2, 1, 120),
  (3, 2, 60);

-- Insert mock data into MOVIMIENTO
INSERT INTO MOVIMIENTO (IDProducto, IDBodega, Tipo, Cantidad, Comentario, UsuarioResponsable)
VALUES
  (1, 1, 'ENTRADA', 80, 'Ingreso inicial de arroz', 'admin'),
  (2, 1, 'ENTRADA', 120, 'Ingreso inicial de frijoles', 'admin'),
  (3, 2, 'ENTRADA', 60, 'Ingreso inicial de azúcar', 'admin');

