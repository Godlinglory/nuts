package net.thevpc.nuts.core.test.bundles.nanodb;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDB;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBTableFile;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestNanoDBTableFile {
    @Test
    public void test1(){
        for (String s : new String[]{"", "a", "ab", "abc"}) {
            System.out.println("getUTFLength(\""+s+"\")="+ NanoDBTableFile.getUTFLength(s));
        }
        System.out.println("getUTFLength(\"Hammadi\")="+ NanoDBTableFile.getUTFLength("Hammadi"));
        NanoDB db=new NanoDB(TestUtils.initFolder(".test-bd").toFile());
        NanoDBTableFile<Person> test=db.tableBuilder(Person.class).setNullable(false).addAllFields().addIndices("id").create();
        test.add(new Person(1,"Hammadi"));
        test.add(new Person(2,"Hammadi"));
        test.add(new Person(1,"Hammadi"));
        test.add(new Person(4,"Hammadi"));
        System.out.println("getFileLength="+test.getFileLength());
        test.findByIndex("id",1).forEach(x->{
            System.out.println(x);
        });
        test.findIndexValues("id").forEach(x->{
            System.out.println(x);
        });
    }

    @Test
    public void testPerf(){
        File dir = TestUtils.initFolder(".test-db-perf").toFile();
        long from = System.currentTimeMillis();
        CoreIOUtils.delete(null,dir);
        try(NanoDB db=new NanoDB(dir)) {
            NanoDBTableFile<Person> test=db.tableBuilder(Person.class).setNullable(false).addIndices("id").create();
            int c = 1000;
            for (int i = 0; i < c * 10; i++) {
                test.add(new Person(i % 10, "Hammadi"));
            }
            long to = System.currentTimeMillis();
            System.out.println(to - from);
            from = System.currentTimeMillis();
            System.out.println(test.findByIndex("id", 1).count());
            to = System.currentTimeMillis();
            System.out.println(to - from);
        }
    }

    public static class Person{
        private int id;
        private String name;

        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Person() {
        }

        public int getId() {
            return id;
        }

        public Person setId(int id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Person setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}