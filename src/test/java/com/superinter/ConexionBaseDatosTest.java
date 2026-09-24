package com.superinter;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConexionBaseDatosTest {

    private static final String BASE_DE_DATOS_ESPERADA = "super-inter";

    @Autowired
    private DataSource dataSource;

    @Test
    void conectaConLaBaseDeDatosDelProyecto() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT current_database()");
             ResultSet resultSet = statement.executeQuery()) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString(1)).isEqualTo(BASE_DE_DATOS_ESPERADA);
        }
    }
}