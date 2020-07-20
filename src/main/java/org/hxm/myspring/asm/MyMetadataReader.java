package org.hxm.myspring.asm;
import org.objectweb.asm.ClassReader;
import org.springframework.core.io.Resource;
import java.io.IOException;

public class MyMetadataReader {
    private static final int PARSING_OPTIONS = ClassReader.SKIP_DEBUG
            | ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES;

    private  Resource resource;

    private final MyAnnotationMetadata annotationMetadata;


    public MyMetadataReader(Resource resource) throws IOException {
        MyVisitor visitor=new MyVisitor(this.getClass().getClassLoader());
        ClassReader classReader=new ClassReader(resource.getInputStream());
        classReader.accept(visitor, PARSING_OPTIONS);
        this.resource = resource;
        this.annotationMetadata = visitor.getMetadata();
    }

    /**
     * 测试用构造函数
     * @param clazz
     */
    public MyMetadataReader(Class<?> clazz){
        ClassReader classReader=null;
        try {
            classReader=new ClassReader(clazz.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MyVisitor visitor=new MyVisitor(this.getClass().getClassLoader());
        classReader.accept(visitor, PARSING_OPTIONS);
        this.annotationMetadata = visitor.getMetadata();
    }

    public MyAnnotationMetadata getAnnotationMetadata(){
        return annotationMetadata;
    }

    public Resource getResource() {
        return resource;
    }
}
