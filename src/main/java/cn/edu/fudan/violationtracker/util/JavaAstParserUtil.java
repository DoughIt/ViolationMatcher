package cn.edu.fudan.violationtracker.util;

import cn.edu.fudan.violationtracker.entity.TwoValue;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author fancying
 * @author Jerry Zhang
 */
@Slf4j
public class JavaAstParserUtil {
    private JavaAstParserUtil() {
        // utility class
    }

    private static final Map<String, CompilationUnit> COMPILATION_UNIT_CACHE = new WeakHashMap<>(8);
    public static Set<String> getAnchors(String absoluteFilePath) throws IOException {
        Set<String> methodsAndFields = new HashSet<>();

        methodsAndFields.addAll(getAllFieldsInFile(absoluteFilePath));

        methodsAndFields.addAll(getAllMethodsInFile(absoluteFilePath));

        methodsAndFields.addAll(getAllClassNamesInFile(absoluteFilePath));

        return methodsAndFields;
    }

    //批量获取缺陷所在方法名和偏移量
    public static List<TwoValue<String, Integer>> findMethodNameAndOffsetList(CompilationUnit compilationUnit, List<Integer> beginLines, List<Integer> endLines) {
        try {
            //当结束行为空 说明并不需要获取方法名以及偏移量
            if (endLines.isEmpty()) {
                return new ArrayList<>();
            }
            List<TwoValue<String, Integer>> ans = new ArrayList<>();
            for (int i = 0; i < beginLines.size(); i++) {
                int beginLine = beginLines.get(i);
                int endLine = endLines.get(i);
                ans.add(findMethodNameAndOffset(compilationUnit, beginLine, endLine));
            }
            return ans;
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    private static TwoValue<String, Integer> findMethodNameAndOffset(CompilationUnit compilationUnit, Integer beginLine, Integer endLine) {
        // 函数
        List<MethodDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class);
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            if (methodDeclaration.getRange().isPresent()) {
                int begin = methodDeclaration.getRange().get().begin.line;
                int end = methodDeclaration.getRange().get().end.line;
                if (beginLine >= begin && endLine <= end) {
                    return new TwoValue<>(methodDeclaration.getSignature().toString(), beginLine - begin);
                }
            }
        }
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        //判断是否是enum
        if (classOrInterfaceDeclarationList.isEmpty()) {
            List<EnumConstantDeclaration> enumConstantDeclarationList = compilationUnit.findAll(EnumConstantDeclaration.class);
            for (EnumConstantDeclaration enumConstantDeclaration : enumConstantDeclarationList) {
                if (enumConstantDeclaration.getRange().isPresent()) {
                    int begin = enumConstantDeclaration.getRange().get().begin.line;
                    int end = enumConstantDeclaration.getRange().get().end.line;
                    if (beginLine >= begin && endLine <= end) {
                        return new TwoValue<>("enum " + enumConstantDeclaration.getNameAsString(), beginLine - begin);
                    }
                }
            }
        } else {
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
                //构造函数
                List<ConstructorDeclaration> constructorDeclarations = classOrInterfaceDeclaration.findAll(ConstructorDeclaration.class);
                for (ConstructorDeclaration constructorDeclaration : constructorDeclarations) {
                    if (constructorDeclaration.getRange().isPresent()) {
                        int begin = constructorDeclaration.getRange().get().begin.line;
                        int end = constructorDeclaration.getRange().get().end.line;
                        if (beginLine >= begin && endLine <= end) {
                            return new TwoValue<>(constructorDeclaration.getSignature().toString(), beginLine - begin);
                        }
                    }
                }
                //字段
                List<FieldDeclaration> fieldDeclarations = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
                for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
                    if (fieldDeclaration.getRange().isPresent()) {
                        int begin = fieldDeclaration.getRange().get().begin.line;
                        int end = fieldDeclaration.getRange().get().end.line;
                        if (beginLine >= begin && endLine <= end) {
                            StringBuilder simpleName = new StringBuilder();
                            for (VariableDeclarator variableDeclarator : fieldDeclaration.getVariables()) {
                                simpleName.append(variableDeclarator.getName());
                                simpleName.append(" ");
                            }
                            return new TwoValue<>(simpleName.toString(), beginLine - begin);
                        }
                    }
                }
            }
            // 类名
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
                if (classOrInterfaceDeclaration.getRange().isPresent()) {
                    int begin = classOrInterfaceDeclaration.getRange().get().begin.line;
                    int end = classOrInterfaceDeclaration.getRange().get().end.line;
                    if (beginLine >= begin && endLine <= end) {
                        return new TwoValue<>("class " + classOrInterfaceDeclaration.getNameAsString(),
                                classOrInterfaceDeclaration.getRange().isPresent() ? beginLine - classOrInterfaceDeclaration.getRange().get().begin.line : beginLine);
                    }
                }
            }
        }
        return new TwoValue<>("", 0);
    }


    private static CompilationUnit analyzeFileToCompilationUnit(String absoluteFilePath) throws IOException {
        if (COMPILATION_UNIT_CACHE.containsKey(absoluteFilePath)) {
            return COMPILATION_UNIT_CACHE.get(absoluteFilePath);
        }
        FileInputStream in = new FileInputStream(absoluteFilePath);
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(in, StandardCharsets.UTF_8);
        if (parseResult.getResult().isPresent()) {
            COMPILATION_UNIT_CACHE.put(absoluteFilePath, parseResult.getResult().get());
            return parseResult.getResult().get();
        }

        return null;
    }

    public static List<String> getAllClassNamesInFile(String absoluteFilePath) throws IOException {
        return new ArrayList<>(getAllClassNamesWithBeginLineInFile(absoluteFilePath).keySet());
    }

    public static Map<String, Integer> getAllClassNamesWithBeginLineInFile(String absoluteFilePath) throws IOException {
        CompilationUnit compileUtil = analyzeFileToCompilationUnit(absoluteFilePath);
        if (compileUtil == null) {
            return Collections.emptyMap();
        }
        Map<String, Integer> allClassNamesInFile = new HashMap<>();

        for (EnumDeclaration e : compileUtil.findAll(EnumDeclaration.class)) {
            allClassNamesInFile.put("enum " + e.getNameAsString(), e.getRange().isPresent() ? e.getRange().get().begin.line : 0);
        }

        for (ClassOrInterfaceDeclaration c : compileUtil.findAll(ClassOrInterfaceDeclaration.class)) {
            allClassNamesInFile.put(c.isInterface() ? "interface " : "class " + c.getNameAsString(),
                    c.getRange().isPresent() ? c.getRange().get().begin.line : 0);
        }

        return allClassNamesInFile;
    }


    /**
     * 抽java文件中所有方法签名
     *
     * @param absoluteFilePath 绝对文件路径
     * @return 所有方法签名
     */
    public static List<String> getAllMethodsInFile(String absoluteFilePath) throws IOException {
        return new ArrayList<>(getAllMethodsWithBeginLineInFile(absoluteFilePath).keySet());
    }

    /**
     * 抽java文件中所有方法签名，以及对应的起始行号
     *
     * @param absoluteFilePath 绝对文件路径
     * @return 所有方法签名 -> 起始行号
     */
    public static Map<String, Integer> getAllMethodsWithBeginLineInFile(String absoluteFilePath) throws IOException {
        var compileUtil = analyzeFileToCompilationUnit(absoluteFilePath);
        if (compileUtil == null) {
            return Collections.emptyMap();
        }
        Map<String, Integer> allMethodsInFile = new HashMap<>();
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compileUtil.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
            List<MethodDeclaration> methodDeclarations = classOrInterfaceDeclaration.findAll(MethodDeclaration.class);
            for (MethodDeclaration methodDeclaration : methodDeclarations) {
                allMethodsInFile.put(methodDeclaration.getSignature().toString(),
                        methodDeclaration.getRange().isPresent() ? methodDeclaration.getRange().get().begin.line : 0);
            }
        }
        return allMethodsInFile;
    }


    /**
     * 抽一个java文件中所有成员变量名
     *
     * @param absoluteFilePath 文件路径
     * @return 成员变量list列表
     */
    public static List<String> getAllFieldsInFile(String absoluteFilePath) throws IOException {
        return new ArrayList<>(getAllFieldsWithBeginLineInFile(absoluteFilePath).keySet());
    }

    /**
     * 抽一个java文件中所有成员变量名
     *
     * @param absoluteFilePath 文件路径
     * @return 成员变量list列表
     */
    public static Map<String, Integer> getAllFieldsWithBeginLineInFile(String absoluteFilePath) throws IOException {
        CompilationUnit compileUtil = analyzeFileToCompilationUnit(absoluteFilePath);
        if (compileUtil == null) {
            return Collections.emptyMap();
        }
        Map<String, Integer> allFieldsInFile = new HashMap<>();
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compileUtil.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
            List<FieldDeclaration> fieldDeclarations = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
            for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
                NodeList<VariableDeclarator> variables = fieldDeclaration.getVariables();
                for (VariableDeclarator variable : variables) {
                    allFieldsInFile.put(variable.getName().toString(),
                            variable.getRange().isPresent() ? variable.getRange().get().begin.line : 0);
                }
            }
        }
        return allFieldsInFile;
    }
}
