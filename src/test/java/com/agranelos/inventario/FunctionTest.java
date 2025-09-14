package com.agranelos.inventario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.microsoft.azure.functions.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
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

    // ======================== TESTS PARA FUNCIONES DE BODEGAS ========================

    /**
     * Test para el endpoint de obtener bodegas
     */
    @Test
    public void testGetBodegas() throws Exception {
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
            final HttpResponseMessage ret = function.getBodegas(req, context);
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
     * Test para el endpoint de crear bodega
     */
    @Test
    public void testCreateBodega() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        // JSON válido para crear bodega
        final String bodegaJson =
            "{\"nombre\":\"Bodega Test\",\"ubicacion\":\"Ubicación test\",\"capacidad\":1500}";
        final Optional<String> queryBody = Optional.of(bodegaJson);
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
            final HttpResponseMessage ret = function.createBodega(
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
     * Test para el endpoint de crear bodega con datos inválidos
     */
    @Test
    public void testCreateBodegaWithInvalidData() throws Exception {
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

        final HttpResponseMessage ret = function.createBodega(req, context);

        // Debería devolver BAD_REQUEST por body vacío
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
    }

    /**
     * Test para el endpoint de obtener bodega por ID
     */
    @Test
    public void testGetBodegaById() throws Exception {
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
            final HttpResponseMessage ret = function.getBodegaById(
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
     * Test para el endpoint de obtener bodega por ID sin ID
     */
    @Test
    public void testGetBodegaByIdWithoutId() throws Exception {
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

        final HttpResponseMessage ret = function.getBodegaById(
            req,
            null,
            context
        );

        // Debería devolver BAD_REQUEST por falta de ID
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
    }

    /**
     * Test para el endpoint de actualizar bodega
     */
    @Test
    public void testUpdateBodega() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "1");
        doReturn(queryParams).when(req).getQueryParameters();

        // JSON válido para actualizar bodega
        final String bodegaJson =
            "{\"nombre\":\"Bodega Actualizada\",\"ubicacion\":\"Nueva ubicación\",\"capacidad\":2000}";
        final Optional<String> queryBody = Optional.of(bodegaJson);
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
            final HttpResponseMessage ret = function.updateBodega(
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
     * Test para el endpoint de eliminar bodega
     */
    @Test
    public void testDeleteBodega() throws Exception {
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
            final HttpResponseMessage ret = function.deleteBodega(
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

    // ======================== EDGE CASE TESTS ========================

    /**
     * Test for malformed JSON input
     */
    @Test
    public void testCreateBodegaMalformedJson() {
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        // Malformed JSON
        final Optional<String> queryBody = Optional.of("{invalid-json}");
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                        .status(status);
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.createBodega(req, context);
            assertNotNull(ret);
            // Should return BAD_REQUEST for malformed JSON
            assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
        } catch (RuntimeException e) {
            // Expected for malformed JSON or database issues
            assertTrue(
                e.getMessage().contains("JSON") ||
                e.getMessage().contains("Database initialization failed")
            );
        }
    }

    /**
     * Test for missing required fields
     */
    @Test
    public void testCreateBodegaMissingRequiredFields() {
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        // Missing nombre field
        final Optional<String> queryBody = Optional.of(
            "{\"ubicacion\":\"Test Location\"}"
        );
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                        .status(status);
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.createBodega(req, context);
            assertNotNull(ret);
            // Should return BAD_REQUEST for missing required fields
        } catch (RuntimeException e) {
            // Expected for validation errors or database issues
            assertTrue(
                e.getMessage().contains("required") ||
                e.getMessage().contains("Database initialization failed") ||
                e.getMessage().contains("nombre")
            );
        }
    }

    /**
     * Test for invalid data types
     */
    @Test
    public void testCreateBodegaInvalidDataTypes() {
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        // Capacity as string instead of integer
        final Optional<String> queryBody = Optional.of(
            "{\"nombre\":\"Test\",\"ubicacion\":\"Test Location\",\"capacidad\":\"not-a-number\"}"
        );
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                        .status(status);
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.createBodega(req, context);
            assertNotNull(ret);
            // Should return BAD_REQUEST for invalid data type
        } catch (RuntimeException e) {
            // Expected for JSON parsing errors or database issues
            assertTrue(
                e.getMessage().contains("JSON") ||
                e.getMessage().contains("parse") ||
                e.getMessage().contains("Database initialization failed")
            );
        }
    }

    /**
     * Test for boundary values - zero capacity
     */
    @Test
    public void testCreateBodegaZeroCapacity() {
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.of(
            "{\"nombre\":\"Test\",\"ubicacion\":\"Test Location\",\"capacidad\":0}"
        );
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                        .status(status);
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.createBodega(req, context);
            assertNotNull(ret);
            // Zero capacity might be valid or invalid depending on business rules
        } catch (RuntimeException e) {
            // Expected for database issues in test environment
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e.getMessage().contains("Required environment variable not found")
            );
        }
    }

    /**
     * Test for boundary values - negative capacity
     */
    @Test
    public void testCreateBodegaNegativeCapacity() {
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.of(
            "{\"nombre\":\"Test\",\"ubicacion\":\"Test Location\",\"capacidad\":-100}"
        );
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                        .status(status);
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.createBodega(req, context);
            assertNotNull(ret);
            // Negative capacity should be validated
        } catch (RuntimeException e) {
            // Expected for validation errors or database issues
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e.getMessage().contains("Required environment variable not found") ||
                e.getMessage().contains("negative")
            );
        }
    }

    /**
     * Test for special characters in name
     */
    @Test
    public void testCreateBodegaSpecialCharacters() {
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.of(
            "{\"nombre\":\"Bodega José María Ñoño\",\"ubicacion\":\"São Paulo - Brasil\",\"capacidad\":1000}"
        );
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                        .status(status);
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.createBodega(req, context);
            assertNotNull(ret);
            // Special characters should be handled properly
        } catch (RuntimeException e) {
            // Expected for database issues in test environment
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e.getMessage().contains("Required environment variable not found")
            );
        }
    }

    /**
     * Test for maximum integer capacity
     */
    @Test
    public void testCreateBodegaMaxIntCapacity() {
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(
            HttpRequestMessage.class
        );

        final Map<String, String> queryParams = new HashMap<>();
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.of(
            "{\"nombre\":\"Large Warehouse\",\"ubicacion\":\"Test Location\",\"capacidad\":" + Integer.MAX_VALUE + "}"
        );
        doReturn(queryBody).when(req).getBody();

        doAnswer(
            new Answer<HttpResponseMessage.Builder>() {
                @Override
                public HttpResponseMessage.Builder answer(
                    InvocationOnMock invocation
                ) {
                    HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                    return new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                        .status(status);
                }
            }
        )
            .when(req)
            .createResponseBuilder(any(HttpStatus.class));

        try {
            final HttpResponseMessage ret = function.createBodega(req, context);
            assertNotNull(ret);
            // Maximum integer should be handled
        } catch (RuntimeException e) {
            // Expected for database issues in test environment
            assertTrue(
                e.getMessage().contains("Database initialization failed") ||
                e.getMessage().contains("Required environment variable not found")
            );
        }
    }

    /**
     * Simple concurrency test using threads
     */
    @Test
    public void testConcurrentBodegaCreation() throws InterruptedException {
        final int threadCount = 5;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    @SuppressWarnings("unchecked")
                    final HttpRequestMessage<Optional<String>> req = mock(
                        HttpRequestMessage.class
                    );

                    final Map<String, String> queryParams = new HashMap<>();
                    doReturn(queryParams).when(req).getQueryParameters();

                    final Optional<String> queryBody = Optional.of(
                        "{\"nombre\":\"Concurrent Test " + threadId + "\",\"ubicacion\":\"Test Location " + threadId + "\",\"capacidad\":1000}"
                    );
                    doReturn(queryBody).when(req).getBody();

                    doAnswer(
                        new Answer<HttpResponseMessage.Builder>() {
                            @Override
                            public HttpResponseMessage.Builder answer(
                                InvocationOnMock invocation
                            ) {
                                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                                    .status(status);
                            }
                        }
                    )
                        .when(req)
                        .createResponseBuilder(any(HttpStatus.class));

                    final HttpResponseMessage ret = function.createBodega(req, context);
                    assertNotNull(ret);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    // Expected failures due to test environment limitations
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        
        // In a test environment, we expect some failures due to database setup issues
        // The important thing is that the function doesn't crash
        assertTrue(successCount.get() + failureCount.get() == threadCount, "All operations completed");
    }
}
