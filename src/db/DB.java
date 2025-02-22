package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DB {

	private static Connection conn = null;

	/*
	 * public static Connection getConnection() { if (conn == null) { try {
	 * Properties props = loadProperties(); String url = props.getProperty("dburl");
	 * conn = DriverManager.getConnection(url, props); } catch (SQLException e) {
	 * throw new DbException(e.getMessage()); } } return conn; }
	 */

	public static Connection getConnection() {
		if (conn == null) {
			try {
				Properties props = loadProperties();
				replaceEnvVariables(props); // Substitui variáveis de ambiente
				String url = props.getProperty("dburl");

				conn = DriverManager.getConnection(url, props);
			} catch (SQLException e) {
				throw new DbException("Erro ao conectar ao banco: " + e.getMessage());
			}
		}
		return conn;
	}

	public static void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			}
		}
	}

	private static Properties loadProperties() {
		try (FileInputStream fs = new FileInputStream("db.properties")) {
			Properties props = new Properties();
			props.load(fs);
			return props;
		} catch (IOException e) {
			throw new DbException(e.getMessage());
		}
	}

	public static void closeStatement(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			}
		}
	}

	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			}
		}
	}

	/**
	 * Substitui variáveis de ambiente no formato ${VARIAVEL} pelas variáveis de
	 * ambiente correspondentes definidas no sistema. Se a variável de ambiente
	 * existir, o valor da propriedade será atualizado com o valor da variável de
	 * ambiente. Caso contrário, o valor original do arquivo de propriedades será
	 * mantido.
	 *
	 * <p>
	 * Exemplo:
	 * </p>
	 * 
	 * <pre>
	 *     Se a propriedade no arquivo for:
	 *     user=${MYSQL_DATASOURCE_USERNAME}
	 *     E a variável de ambiente MYSQL_DATASOURCE_USERNAME for "root",
	 *     o valor da propriedade será alterado para "root".
	 * </pre>
	 * 
	 * @param props O objeto {@link Properties} que contém as chaves e valores do
	 *              arquivo de propriedades. As variáveis no formato ${VARIAVEL}
	 *              serão substituídas pelos valores das variáveis de ambiente.
	 */
	private static void replaceEnvVariables(Properties props) {
		props.forEach((key, value) -> {
			String val = (String) value;
			if (val.startsWith("${") && val.endsWith("}")) {
				String envVar = val.substring(2, val.length() - 1); // Extrai o nome da variável
				String envValue = System.getenv(envVar); // Busca no ambiente

				// Se a variável de ambiente existir, substitui; senão, mantém o valor original
				// do arquivo
				props.setProperty((String) key, envValue != null ? envValue : val);
			}
		});
	}

}
