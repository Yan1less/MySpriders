package com.yan1less.quartz;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.minBy;

public class MyJob  {
    @Test
    public void testJob(){
      List<String> list = Arrays.asList("hello"," thank you "," thank you very much ");


      student s1 = new student("a",100);
      student s2 = new student("b",90);
      student s3 = new student("c",80);
      List<student> students = Arrays.asList(s1,s2,s3);
        Optional<student> collect =
                students.stream().collect(minBy(Comparator.comparing(student::getScore)));
  students.stream().map(student::getName).forEach(System.out::println);
        System.out.println(Runtime.getRuntime().availableProcessors());

    }
    public void Cibiton(List<Integer> list, Predicate<Integer> predicate){
        for (Integer integer : list){
            if(predicate.test(integer)){
                System.out.println(integer);
            }
        }
    }

}
