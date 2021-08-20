package com.hxm.myspring.aop;


import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspects {

    //公共切点表达式
    @Pointcut("execution(public int IMathCalculator.*(..))")
    public void pointCut(){

    }

    //1、可以直接写切点表达式
    @Before("execution(public int IMathCalculator.*(..))")
    public void logStart(){
        System.out.println("除法运行，参数列表是：{}");
    }

    //2、也可以写公共的切点函数，在本类中直接写方法名
    @After(value = "pointCut()")
    public void logEnd(){
        System.out.println("除法结束");
    }

    //3、外部类写公共切点函数的全类名
    @AfterReturning(value = "com.hxm.myspring.aop.LogAspects.pointCut()",returning = "result")
    public void logReturn(Object result){
        System.out.println("除法正常返回，运行结果：{"+result+"}");
    }

    @AfterThrowing(value = "pointCut()",throwing = "exception")
    public void logException(Exception exception){
        System.out.println("除法异常，异常信息：{}");
    }
}
