package com.crunchbase.codegen;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.*;
import org.openapitools.codegen.meta.features.*;
import org.openapitools.codegen.templating.mustache.*;
import org.openapitools.codegen.utils.ModelUtils;

import java.io.File;
import java.util.*;

public class CirceGenerator extends DefaultCodegen implements CodegenConfig {

  // source folder where to write the files
  protected String sourceFolder = "src/main/scala";
  protected String packageName = "com.crunchbase";

  public CirceGenerator() {
    super();

    featureSet = getFeatureSet().modify()
                .includeDocumentationFeatures(DocumentationFeature.Readme)
                .wireFormatFeatures(EnumSet.of(WireFormatFeature.JSON, WireFormatFeature.XML, WireFormatFeature.Custom))
                .securityFeatures(EnumSet.noneOf(SecurityFeature.class))
                .excludeGlobalFeatures(
                        GlobalFeature.XMLStructureDefinitions,
                        GlobalFeature.Callbacks,
                        GlobalFeature.LinkObjects,
                        GlobalFeature.ParameterStyling
                )
                .excludeSchemaSupportFeatures(
                        SchemaSupportFeature.Polymorphism
                )
                .excludeParameterFeatures(
                        ParameterFeature.Cookie
                )
                .build();

    // set the output folder here
    outputFolder = "generated-code/circe";

    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    modelTemplateFiles.put("model.mustache", ".scala");

    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "circe";

    modelPackage = packageName + ".model";

    setReservedWordsLowerCase(
      Arrays.asList(
        // Scala
        "abstract", "case", "catch", "class", "def",
        "do", "else", "extends", "false", "final",
        "finally", "for", "forSome", "if", "implicit",
        "import", "lazy", "match", "new", "null",
        "object", "override", "package", "private", "protected",
        "return", "sealed", "super", "this", "throw",
        "trait", "try", "true", "type", "val",
        "var", "while", "with", "yield",
        // Scala-interop languages keywords
        "abstract", "continue", "switch", "assert",
        "default", "synchronized", "goto",
        "break", "double", "implements", "byte",
        "public", "throws", "enum", "instanceof", "transient",
        "int", "short", "char", "interface", "static",
        "void", "finally", "long", "strictfp", "volatile", "const", "float",
        "native")
    );

    defaultIncludes = new HashSet<String>(
      Arrays.asList("double",
        "Int",
        "Long",
        "Float",
        "Double",
        "char",
        "float",
        "String",
        "boolean",
        "Boolean",
        "Double",
        "Integer",
        "Long",
        "Float",
        "List",
        "Set",
        "Map")
    ) ;

    typeMapping = new HashMap<String, String>();
    typeMapping.put("string", "String");
    typeMapping.put("boolean", "Boolean");
    typeMapping.put("integer", "Int");
    typeMapping.put("float", "Float");
    typeMapping.put("long", "Long");
    typeMapping.put("double", "Double");
    typeMapping.put("number", "BigDecimal");
    typeMapping.put("date-time", "OffsetDateTime"); // ZonedDateTime?
    typeMapping.put("date", "LocalDateTime");
    typeMapping.put("file", "File");
    typeMapping.put("array", "Seq");
    typeMapping.put("list", "List");
    typeMapping.put("map", "Map");
    typeMapping.put("object", "Object");
    typeMapping.put("binary", "Array[Byte]");
    typeMapping.put("Date", "LocalDateTime");
    typeMapping.put("DateTime", "OffsetDateTime"); // ZonedDateTime?

    /**
     * Additional Properties.  These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("modelPackage", modelPackage());
    additionalProperties.put("titlecase", new TitlecaseLambda());
    additionalProperties.put("lowercase", new LowercaseLambda());
    additionalProperties.put("modelPackage", modelPackage());

    if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
      setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
    } else {
      additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
    }

    /**
     * Supporting Files.  You can write single files for the generator with the
     * entire object tree available.  If the input file has a suffix of `.mustache
     * it will be processed by the template engine.  Otherwise, it will be copied
     */
    supportingFiles.add(new SupportingFile("gitignore.mustache", sourceFolder, ".gitignore"));
    supportingFiles.add(new SupportingFile("licenseInfo.mustache", sourceFolder, "licenseInfo"));

    /**
     * Language Specific Primitives.  These types will not trigger imports by
     * the client generator
     */
    languageSpecificPrimitives = new HashSet<String>(
      Arrays.asList(
        "String",
        "Boolean",
        "Double",
        "Int",
        "Integer",
        "Long",
        "Float",
        "Any",
        "AnyVal",
        "AnyRef",
        "Object")
    );

    instantiationTypes.put("array", "ArrayList");
    instantiationTypes.put("map", "HashMap");

    importMapping = new HashMap<String, String>();
    importMapping.put("BigDecimal", "java.math.BigDecimal");
    importMapping.put("UUID", "java.util.UUID");
    importMapping.put("File", "java.io.File");
    importMapping.put("Date", "java.util.Date");
    importMapping.put("Timestamp", "java.sql.Timestamp");
    importMapping.put("Map", "scala.collection.immutable.Map");
    importMapping.put("HashMap", "scala.collection.immutable.HashMap");
    importMapping.put("Seq", "scala.collection.immutable.Seq");
    importMapping.put("ArrayBuffer", "scala.collection.mutable.ArrayBuffer");
    importMapping.put("DateTime", "java.time.LocalDateTime");
    importMapping.put("LocalDateTime", "java.time.LocalDateTime");
    importMapping.put("LocalDate", "java.time.LocalDate");
    importMapping.put("LocalTime", "java.time.LocalTime");
    importMapping.put("OffsetDateTime", "java.time.OffsetDateTime");

    cliOptions.clear();
    cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Models package name (e.g. org.openapitools).")
      .defaultValue(this.packageName));
    cliOptions.add(new CliOption(CodegenConstants.MODEL_PACKAGE, CodegenConstants.MODEL_PACKAGE_DESC));
  }

  /**
   * Configures the type of generator.
   *
   * @return  the CodegenType for this generator
   * @see     org.openapitools.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.OTHER;
  }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -g flag.
   *
   * @return the friendly name for the generator
   */
  public String getName() {
    return "circe";
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   *
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a circe client library.";
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reserved words
   *
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    return "_" + name;
  }

  /**
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + File.separator + sourceFolder + File.separator + modelPackage().replace('.', File.separatorChar);
  }

  // /**
  //  * Convert Swagger Model object to Codegen Model object
  //  *
  //  * @param name           the name of the model
  //  * @param model          Swagger Model object
  //  * @param allDefinitions a map of all Swagger models from the spec
  //  * @return Codegen Model object
  //  */
  // @Override
  // public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
  //     CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);
  //     return codegenModel;
  // }

  @Override
  public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
    Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
    List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
    for (CodegenOperation op : operationList) {

      // Converts GET /foo/bar => get("foo" :: "bar")
      generateScalaPath(op);

      // Generates e.g. uuid :: header("boo") :: params("baa") under key "x-codegen-pathParams"
      // Generates e.g. (id: UUID, headerBoo: String, paramBaa: String) under key "x-codegen-typedInputParams"
      // Generates e.g. (id, headerBoo, paramBaa) under key "x-codegen-inputParams"
      generateInputParameters(op);

      //Generate Auth parameters using security: definition
      //Results in header("apiKey") or param("apiKey")
      authParameters(op);

      //Concatenates all parameters
      concatParameters(op);
    }
    return objs;
  }

  /**
   * Optional - type declaration.  This is a String which is used by the templates to instantiate your
   * types.  There is typically special handling for different property types
   *
   * @return a string value used as the `dataType` field for model templates, `returnType` for api templates
   */
  @SuppressWarnings("Duplicates")
  @Override
  public String getTypeDeclaration(Schema p) {
    if (ModelUtils.isArraySchema(p)) {
      ArraySchema ap = (ArraySchema) p;
      Schema inner = ap.getItems();
      return getSchemaType(p) + "[" + getTypeDeclaration(inner) + "]";
    } else if (ModelUtils.isMapSchema(p)) {
        Schema inner = ModelUtils.getAdditionalProperties(p);

        return getSchemaType(p) + "[String, " + getTypeDeclaration(inner) + "]";
    }
    return super.getTypeDeclaration(p);
  }

  @Override
  public String getSchemaType(Schema p) {
    String schemaType = super.getSchemaType(p);
    String type;
    if (typeMapping.containsKey(schemaType)) {
      type = typeMapping.get(schemaType);
      if (languageSpecificPrimitives.contains(type)) {
        return toModelName(type);
      }
    } else {
      type = schemaType;
    }
    return toModelName(type);
  }

  /**
   * Escape single and/or double quote to avoid code injection
   *
   * @param input String to be cleaned up
   * @return string with quotation mark removed or escaped
   */
  @Override
  public String escapeQuotationMark(String input) {
    // remove " to avoid code injection
    return input.replace("\"", "");
  }

  @Override
  public String escapeUnsafeCharacters(String input) {
    return input.replace("*/", "*_/").replace("/*", "/_*");
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  /**
     * @param prim          primitive type
     * @param isRequired    true if it's required
     * @param canBeOptional true if it can be optional
     * @return string representation of the primitive type
     */
    private String toPrimitive(String prim, Boolean isRequired, Boolean canBeOptional) {

      String converter = ".map(_.to" + prim + ")";
      return (canBeOptional ? (isRequired ? converter : ".map(_" + converter + ")") : "");
  }

  //All path parameters are String initially, for primitives these need to be converted
  private String toPathParameter(CodegenParameter p, String paramType, Boolean canBeOptional) {
    Boolean isNotAString = !p.dataType.equals("String");
    return paramType + (canBeOptional && !p.required ? "Option" : "") + "(\"" + p.baseName + "\")" + (isNotAString ? toPrimitive(p.dataType, p.required, canBeOptional) : "");
  }

  private String toInputParameter(CodegenParameter p) {
    return (p.required ? "" : "Option[") + p.dataType + (p.required ? "" : "]");
  }

  private String concat(String original, String addition, String op) {
    return original + (original.isEmpty() ? "" : (addition.isEmpty() ? "" : op)) + addition;
  }

  // a, b
  private String csvConcat(String original, String addition) {
    return concat(original, addition, ", ");
  }

  // a :: b
  private String colConcat(String original, String addition) {
    return concat(original, addition, " :: ");
  }

  private void authParameters(CodegenOperation op) {
    String authParams = "";
    String authInputParams = "";
    String typedAuthInputParams = "";
    //Append apikey security to path params and create input parameters for functions
    if (op.authMethods != null) {
      for (CodegenSecurity s : op.authMethods) {
        if (s.isApiKey && s.isKeyInHeader) {
          authParams = colConcat(authParams, "header(\"" + s.keyParamName + "\")");
        } else if (s.isApiKey && s.isKeyInQuery) {
          authParams = colConcat(authParams, "param(\"" + s.keyParamName + "\")");
        }
        if (s.isApiKey) {
          typedAuthInputParams = csvConcat(typedAuthInputParams, "authParam" + s.name + ": String");
          authInputParams = csvConcat(authInputParams, "authParam" + s.name);
        }
      }
    }

    op.vendorExtensions.put("x-codegen-authParams", authParams);
    op.vendorExtensions.put("x-codegen-authInputParams", authInputParams);
    op.vendorExtensions.put("x-codegen-typedAuthInputParams", typedAuthInputParams);
  }

  private void generateScalaPath(CodegenOperation op) {
    op.httpMethod = op.httpMethod.toLowerCase(Locale.ROOT);

    String path = op.path;

    // remove first /
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    // remove last /
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    String[] items = path.split("/", -1);
    String scalaPath = "";
    Integer pathParamIndex = 0;

    for (String item : items) {
      if (item.matches("^\\{(.*)}$")) { // wrap in {}
        // find the datatype of the parameter
        final CodegenParameter cp = op.pathParams.get(pathParamIndex);

        // TODO: Handle non-primitives…
        scalaPath = colConcat(scalaPath, cp.dataType.toLowerCase(Locale.ROOT));

        pathParamIndex++;
      } else {
        scalaPath = colConcat(scalaPath, "\"" + item + "\"");
      }
    }

    op.vendorExtensions.put("x-codegen-path", scalaPath);

  }

  private void concatParameters(CodegenOperation op) {

    String path = colConcat(colConcat(op.vendorExtensions.get("x-codegen-path").toString(), op.vendorExtensions.get("x-codegen-pathParams").toString()), op.vendorExtensions.get("x-codegen-authParams").toString());
    String parameters = csvConcat(op.vendorExtensions.get("x-codegen-inputParams").toString(), op.vendorExtensions.get("x-codegen-authInputParams").toString());
    String typedParameters = csvConcat(op.vendorExtensions.get("x-codegen-typedInputParams").toString(), op.vendorExtensions.get("x-codegen-typedAuthInputParams").toString());

    // The input parameters for functions
    op.vendorExtensions.put("x-codegen-paths", path);
    op.vendorExtensions.put("x-codegen-params", parameters);
    op.vendorExtensions.put("x-codegen-typedParams", typedParameters);

  }

  private void generateInputParameters(CodegenOperation op) {

    String inputParams = "";
    String typedInputParams = "";
    String pathParams = "";

    for (CodegenParameter p : op.allParams) {
      // TODO: This hacky, should be converted to mappings if possible to keep it clean.
      // This could also be done using template imports

      if (p.isBodyParam) {
        p.vendorExtensions.put("x-codegen-normalized-path-type", "jsonBody[" + p.dataType + "]");
        p.vendorExtensions.put("x-codegen-normalized-input-type", p.dataType);
      } else if (p.isContainer || p.isListContainer) {
        p.vendorExtensions.put("x-codegen-normalized-path-type", toPathParameter(p, "params", false));
        p.vendorExtensions.put("x-codegen-normalized-input-type", p.dataType.replaceAll("^[^\\[]+", "Seq"));
      } else if (p.isQueryParam) {
        p.vendorExtensions.put("x-codegen-normalized-path-type", toPathParameter(p, "param", true));
        p.vendorExtensions.put("x-codegen-normalized-input-type", toInputParameter(p));
      } else if (p.isHeaderParam) {
        p.vendorExtensions.put("x-codegen-normalized-path-type", toPathParameter(p, "header", true));
        p.vendorExtensions.put("x-codegen-normalized-input-type", toInputParameter(p));
      } else if (p.isFile) {
        p.vendorExtensions.put("x-codegen-normalized-path-type", "fileUpload(\"" + p.paramName + "\")");
        p.vendorExtensions.put("x-codegen-normalized-input-type", "FileUpload");
      } else if (p.isPrimitiveType && !p.isPathParam) {
        if (!p.required) {
          // Generator's current version of Finch doesn't support something like stringOption, but finch aggregates all
          // parameter types under "param", so optional params can be grabbed by "paramOption".
          p.vendorExtensions.put("x-codegen-normalized-path-type", toPathParameter(p, "param", true));
        } else {
          // If parameter is primitive and required, we can rely on data types like "string" or "long"
          p.vendorExtensions.put("x-codegen-normalized-path-type", p.dataType.toLowerCase(Locale.ROOT));
        }
        p.vendorExtensions.put("x-codegen-normalized-input-type", toInputParameter(p));
      } else {
          //Path paremeters are handled in generateScalaPath()
        p.vendorExtensions.put("x-codegen-normalized-input-type", p.dataType);
      }
      if (p.vendorExtensions.get("x-codegen-normalized-path-type") != null) {
        pathParams = colConcat(pathParams, p.vendorExtensions.get("x-codegen-normalized-path-type").toString());
      }
      inputParams = csvConcat(inputParams, p.paramName);
      typedInputParams = csvConcat(typedInputParams, p.paramName + ": " + p.vendorExtensions.get("x-codegen-normalized-input-type"));
    }

    // All body, path, query and header parameters
    op.vendorExtensions.put("x-codegen-pathParams", pathParams);

    // The input parameters for functions
    op.vendorExtensions.put("x-codegen-inputParams", inputParams);
    op.vendorExtensions.put("x-codegen-typedInputParams", typedInputParams);

  }

}