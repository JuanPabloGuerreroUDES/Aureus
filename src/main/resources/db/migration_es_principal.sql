-- Migración: campos nuevos en la tabla cuentas.
-- Ejecutar UNA sola vez si la BD ya existe.

ALTER TABLE cuentas ADD COLUMN IF NOT EXISTS es_principal TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE cuentas ADD COLUMN IF NOT EXISTS icono VARCHAR(10) NULL;
ALTER TABLE cuentas ADD COLUMN IF NOT EXISTS descripcion VARCHAR(255) NULL;

-- Marcar la cuenta más antigua de cada usuario como principal
UPDATE cuentas c
INNER JOIN (
    SELECT usuario_id, MIN(id) AS id_principal
    FROM cuentas
    GROUP BY usuario_id
) primera ON c.usuario_id = primera.usuario_id AND c.id = primera.id_principal
SET c.es_principal = 1;
