package es.iesisidra.datastock.componentes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionConfig;
import es.iesisidra.datastock.modelo.ProductoConsultaDTO;

public class ConsultaProductoJdbc {
    private static final String SQL_BUSCAR_POR_CODIGO = "SELECT p.codigo, p.nombre, p.precio, p.stock, "
            + "c.nombre AS categoria, pr.nombre AS proveedor "
            + "FROM producto p "
            + "JOIN categoria c ON p.categoria_id = c.id "
            + "JOIN proveedor pr ON p.proveedor_id = pr.id "
            + "WHERE p.codigo = ?";

    
    public ProductoConsultaDTO buscarPorCodigo(String codigo) throws SQLException {
        ProductoConsultaDTO producto = null;

        try (Connection conexion = ConexionConfig.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_CODIGO)) {

            sentencia.setString(1, codigo);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    producto = new ProductoConsultaDTO(
                            resultado.getString("codigo"),
                            resultado.getString("nombre"),
                            resultado.getBigDecimal("precio"),
                            resultado.getInt("stock"),
                            resultado.getString("categoria"),
                            resultado.getString("proveedor"));
                }
            }
        }

        return producto;
    }
}
