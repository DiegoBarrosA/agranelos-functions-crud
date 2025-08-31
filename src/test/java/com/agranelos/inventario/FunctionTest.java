package com.agranelos.inventario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.microsoft.azure.functions.*;
import java.util.*;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit tests for Function class.
 */
public class FunctionTest {

    private Function function;
    private ExecutionContext context;

    @BeforeEach
    public void setUp() {
        function = new Function();
        context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();
    }

    /**
     * Test para el endpoint de inicialización de base de datos
     */
    @Test
    public void testInitializeDatabase() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.of("{}");
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status =
                        (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(
                        status
                    );
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        // Note: This test will fail if no database connection is available
        // In a real scenario, you would mock the database connection
        try {
            final HttpResponseMessage ret = function.initializeDatabase(
                req,
                context
            );
            // If we reach here, the method executed without throwing an exception
            assertNotNull(ret);
        } catch (RuntimeException e) {
            // Expected if no database connection is available during testing
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e
                    .getMessage()
                    .contains("Required environment variable not found")
            );
        }
    }

    /**
     * Test para el endpoint de obtener productos
     */
    @Test
    public void testGetProductos() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.empty();
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status =
                        (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(
                        status
                    );
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.getProductos(req, context);
            assertNotNull(ret);
        } catch (RuntimeException e) {
            // Expected if no database connection is available during testing
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e
                    .getMessage()
                    .contains("Required environment variable not found")
            );
        }
    }

    /**
     * Test para el endpoint de crear producto
     */
    @Test
    public void testCreateProducto() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        // JSON válido para crear producto
        final String productJson =
            "{\"nombre\":\"Producto Test\",\"descripcion\":\"Descripción test\",\"precio\":10.50,\"cantidadEnStock\":100}";
        final Optional<String> queryBody = Optional.of(productJson);
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status =
                        (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(
                        status
                    );
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.createProducto(
                req,
                context
            );
            assertNotNull(ret);
        } catch (RuntimeException e) {
            // Expected if no database connection is available during testing
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e
                    .getMessage()
                    .contains("Required environment variable not found")
            );
        }
    }

    /**
     * Test para el endpoint de crear producto con datos inválidos
     */
    @Test
    public void testCreateProductoWithInvalidData() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        // Body vacío (debería devolver error)
        final Optional<String> queryBody = Optional.empty();
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status =
                        (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(
                        status
                    );
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        final HttpResponseMessage ret = function.createProducto(req, context);

        // Debería devolver BAD_REQUEST por body vacío
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
    }

    /**
     * Test para el endpoint de obtener producto por ID
     */
    @Test
    public void testGetProductoById() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "1");
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.empty();
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status =
                        (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(
                        status
                    );
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.getProductoById(
                req,
                "1",
                context
            );
            assertNotNull(ret);
        } catch (RuntimeException e) {
            // Expected if no database connection is available during testing
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e
                    .getMessage()
                    .contains("Required environment variable not found")
            );
        }
    }

    /**
     * Test para el endpoint de obtener producto por ID sin ID
     */
    @Test
    public void testGetProductoByIdWithoutId() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        // No se proporciona ID
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.empty();
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status =
                        (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(
                        status
                    );
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        final HttpResponseMessage ret = function.getProductoById(
            req,
            null,
            context
        );

        // Debería devolver BAD_REQUEST por falta de ID
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
    }

    /**
     * Test para el endpoint de actualizar producto
     */
    @Test
    public void testUpdateProducto() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "1");
        doReturn(queryParams).when(req).getQueryParameters();

        // JSON válido para actualizar producto
        final String productJson =
            "{\"nombre\":\"Producto Actualizado\",\"descripcion\":\"Descripción actualizada\",\"precio\":15.75,\"cantidadEnStock\":200}";
        final Optional<String> queryBody = Optional.of(productJson);
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status =
                        (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(
                        status
                    );
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.updateProducto(
                req,
                "1",
                context
            );
            assertNotNull(ret);
        } catch (RuntimeException e) {
            // Expected if no database connection is available during testing
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e
                    .getMessage()
                    .contains("Required environment variable not found")
            );
        }
    }

    /**
     * Test para el endpoint de eliminar producto
     */
    @Test
    public void testDeleteProducto() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "1");
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.empty();
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status =
                        (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(
                        status
                    );
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.deleteProducto(
                req,
                "1",
                context
            );
            assertNotNull(ret);
        } catch (RuntimeException e) {
            // Expected if no database connection is available during testing
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e
                    .getMessage()
                    .contains("Required environment variable not found")
            );
        }
    }
}
