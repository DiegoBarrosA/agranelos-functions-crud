package com.agranelos.inventario.graphql;

import com.agranelos.inventario.db.DatabaseInitializer;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

/**
 * GraphQL Schema Builder para configurar el esquema y resolvers
 */
public class GraphQLSchemaBuilder {
    
    private static final Logger logger = Logger.getLogger(GraphQLSchemaBuilder.class.getName());
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static GraphQL graphQL;
    
    /**
     * Construye y configura el esquema GraphQL
     */
    public static GraphQL buildSchema() throws IOException {
        if (graphQL == null) {
            synchronized (GraphQLSchemaBuilder.class) {
                if (graphQL == null) {
                    logger.info("Inicializando esquema GraphQL...");
                    
                    // Leer el esquema desde el archivo
                    String schemaString = loadSchemaFromClasspath("schema.graphqls");
                    
                    // Crear el registry de definiciones de tipos
                    SchemaParser schemaParser = new SchemaParser();
                    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaString);
                    
                    // Configurar los Runtime Wirings (resolvers)
                    RuntimeWiring runtimeWiring = buildWiring();
                    
                    // Generar el esquema GraphQL
                    SchemaGenerator schemaGenerator = new SchemaGenerator();
                    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(
                        typeDefinitionRegistry, 
                        runtimeWiring
                    );
                    
                    // Crear la instancia GraphQL
                    graphQL = GraphQL.newGraphQL(graphQLSchema).build();
                    
                    logger.info("Esquema GraphQL inicializado exitosamente");
                }
            }
        }
        return graphQL;
    }
    
    /**
     * Configura los Runtime Wirings (mapping de campos a DataFetchers)
     */
    private static RuntimeWiring buildWiring() {
        return newRuntimeWiring()
            // Query resolvers
            .type("Query", builder -> builder
                .dataFetcher("productos", ProductoDataFetcher.getProductos())
                .dataFetcher("producto", ProductoDataFetcher.getProducto())
                .dataFetcher("bodegas", BodegaDataFetcher.getBodegas())
                .dataFetcher("bodega", BodegaDataFetcher.getBodega())
                .dataFetcher("health", new StaticDataFetcher("GraphQL API funcionando correctamente"))
            )
            // Mutation resolvers  
            .type("Mutation", builder -> builder
                .dataFetcher("crearProducto", ProductoDataFetcher.crearProducto())
                .dataFetcher("actualizarProducto", ProductoDataFetcher.actualizarProducto())
                .dataFetcher("eliminarProducto", ProductoDataFetcher.eliminarProducto())
                .dataFetcher("crearBodega", BodegaDataFetcher.crearBodega())
                .dataFetcher("actualizarBodega", BodegaDataFetcher.actualizarBodega())
                .dataFetcher("eliminarBodega", BodegaDataFetcher.eliminarBodega())
                .dataFetcher("inicializarBaseDatos", dataFetchingEnvironment -> {
                    try {
                        ensureDatabaseInitialized();
                        return "Base de datos inicializada correctamente";
                    } catch (Exception e) {
                        logger.severe("Error inicializando base de datos: " + e.getMessage());
                        return "Error inicializando base de datos: " + e.getMessage();
                    }
                })
            )
            .build();
    }
    
    /**
     * Ejecuta una consulta GraphQL
     */
    public static ExecutionResult executeQuery(String query, String variables, String operationName) throws IOException {
        GraphQL graphQL = buildSchema();
        
        ExecutionInput.Builder inputBuilder = ExecutionInput.newExecutionInput()
            .query(query);
            
        if (variables != null && !variables.trim().isEmpty()) {
            // En un caso real, aquí parsearíamos el JSON de variables
            // Para simplicidad, asumimos que no hay variables complejas por ahora
        }
        
        if (operationName != null && !operationName.trim().isEmpty()) {
            inputBuilder.operationName(operationName);
        }
        
        return graphQL.execute(inputBuilder.build());
    }
    
    /**
     * Carga el esquema GraphQL desde el classpath
     */
    private static String loadSchemaFromClasspath(String path) throws IOException {
        try (InputStream inputStream = GraphQLSchemaBuilder.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("No se pudo encontrar el archivo de esquema: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Asegura que la base de datos esté inicializada (thread-safe)
     */
    private static void ensureDatabaseInitialized() throws Exception {
        if (!initialized.get()) {
            synchronized (GraphQLSchemaBuilder.class) {
                if (!initialized.get()) {
                    logger.info("Inicializando base de datos...");
                    
                    // Inicializar esquema y datos
                    DatabaseInitializer.initializeDatabase();
                    
                    initialized.set(true);
                    logger.info("Base de datos inicializada exitosamente");
                }
            }
        }
    }
}