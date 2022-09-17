package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * 代码生成器
 */
public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Useage:generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
//        defineAst(outputDir, "Expr", Arrays.asList(
//           "Binary   : Expr left,Token operator,Expr right",
//           "Grouping : Expr expression",
//           "Literal  : Object value",
//           "Unary    : Token operator,Expr right"
//        ));
        defineAst(outputDir,"Stmt",Arrays.asList(
                "Expression : Expr expression",
                "Print      : Expr expression"
        ));
    }

    /**
     * 生成基类
     * @param outputDir
     * @param baseName
     * @param types
     * @throws IOException
     */
    private static void defineAst(
            String outputDir, String baseName, List<String> types)
        throws IOException {
        String path = outputDir + "/" +baseName +".java";
        PrintWriter writer = new PrintWriter(path,"UTF-8");
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName +"{");

        //定义visitor类
        defineVisitor(writer, baseName, types);

        for (String type: types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer,baseName,className,fields);
        }
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");
        writer.println("}");
        writer.close();
    }

    /**
     * 定义visitor类
     * @param writer
     * @param baseName
     * @param types
     */
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(" interface Visitor<R> {");
        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit"+typeName +baseName +"("+
                    typeName +" "+baseName.toLowerCase()+");");
        }
        writer.println(" }");
    }
    /**
     * 子类
     * @param writer
     * @param baseName
     * @param className
     * @param fieldList
     */
    private static void defineType(PrintWriter writer,String baseName,String className,String fieldList) {
        writer.println(" static class "+className+" extends "+baseName+" {");
        String[] fields = fieldList.split(",");
        //变量
        for (String field : fields) {
            writer.println("    final "+field+";");
        }

        //构造器
        writer.println("    "+className+"("+fieldList+"){");
        for (String field:fields) {
            String name = field.split(" ")[1];
            writer.println("      this."+name+" = "+name+";");
        }
        writer.println("    }");
        writer.println();
        writer.println("    @Override");
        //继承父类方法
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit"+
                className+baseName+"(this);");
        writer.println("    }");
        writer.println();
        writer.println(" }");
    }
}
