package shen;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandle;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static shen.Shen.*;

public class ShenTest {
    @Test
    public void equals() {
        is(2, "2");
        is(true, "true");
        is("foo", "\"foo\"");
        is(intern("bar"), "bar");
        is(false, "(= 2 3)");
        is(false, "(= \"foo\" \"bar\")");
        is(false, "(= true false)");
        is(false, "(= foo bar)");
    }

    @Test
    public void defun_lambda_and_let() {
        is(intern("f"), "(defun f (x) (lambda y (+ x y))");
        is(5, "((f 3) 2)");
        is(10, "(let x 5 (* 2 x)");
    }

    @Test
    public void errors() {
        is(1.0, "(/ 1 1)");
        is(-1, "(trap-error (/ 1 0) (lambda E -1))");
        is(1.0, "(trap-error (/ 1 1) (lambda E -1))");
        is("testError", "(trap-error (set newError (simple-error \"testError\")) (lambda E (error-to-string E)))");
    }

    @Test
    public void addition() {
        is(5, "(+ 2 3)");
        is(-1, "(+ 2 -3)");
    }

    @Test
    public void subtraction() {
        is(-1, "(- 2 3)");
        is(5, "(- 2 -3)");
    }

    @Test
    public void multiplication() {
        is(6, "(* 2 3)");
        is(-6, "(* 2 -3)");
    }

    @Test
    public void division() {
        is(2.5, "(/ 5 2)");
        is(-1.0, "(/ 4 -4)");
    }

    @Test
    public void less_than() {
        is(true, "(< 1 2)");
        is(false, "(< 4 4)");
        is(false, "(< 4 0)");
    }

    @Test
    public void less_than_or_equal() {
        is(true, "(<= 1 2)");
        is(true, "(<= 4 4)");
        is(false, "(< 4 0)");
    }

    @Test
    public void greater_than() {
        is(true, "(> 3 2)");
        is(false, "(> 4 4)");
        is(false, "(> 2 4)");
    }

    @Test
    public void greater_than_or_equal() {
        is(true, "(>= 3 2)");
        is(true, "(>= 4 4)");
        is(false, "(>= 2 4)");
    }

    @Test
    public void _if() {
        is(true, "(if true true false)");
        is(true, "(if false false true)");
        is(-1, "(trap-error (if 5 true true) (lambda _ -1)");
    }

    @Test
    public void number_p() {
        is(true, "(number? 3)");
        is(true, "(number? 3.4)");
        is(false, "(number? \"fuu\")");
        is(false, "(number? bar)");
    }

    @Test
    public void and() {
        is(true, "(and true true true)");
        is(false, "(and true false true)");
        is(-1, "(trap-error (and 2 true) (lambda E -1)");
    }

    @Test
    public void or() {
        is(true, "(or true true true)");
        is(true, "(or true false true)");
        is(true, "(or (and false false false) true)");
        is(-1, "(trap-error (or 2 true) (lambda E -1)");
    }

    @Test
    public void value() {
        is(5, "(set x 5)");
        is(5, "(value x)");
        is("foo", "(set x \"foo\")");
        is("foo", "(value x)");
        is(-1, "(trap-error (value valueTest) (lambda E -1)");
    }

    @Test
    public void string_p_str_tlstr_cn_and_pos() {
        is(true, "(string? \"bar\")");
        is(true, "(string? (str \"foo\")");
        is(true, "(string? (str 2)");
        is(true, "(string? (str true)");
        is(true, "(string? (str bar)");
        is("bar", "(str bar)");
        is(false, "(string? 55)");
        is("oobar", "(tlstr \"foobar\")");
        is(-1, "(trap-error (cn bla blub) (lambda _ -1)");
        is("foobar", "(cn \"foo\" \"bar\")");
        is("r", "(pos \"bar\" 2)");
    }

    @Test
    public void cons_p() {
        is(true, "(cons? '(2 1 1 \"foo\")");
        is(true, "(cons? (cons 3 '(2 1 1 \"foo\"))");
        is(false, "(cons? 53)");
        is(2, "(hd '(2 1 1)");
        is(1, "(hd (tl '(2 1 1))");
    }

    @Test
    public void absvector_absvector_p_address_gt_and_lt_address() {
        Symbol fail = intern("fail!");
        Object[] absvector = {fail, fail, fail, fail, fail};
        is(absvector, "(set v (absvector 5)");
        is(false, "(absvector? v)");
        is(false, "(absvector? 2)");
        is(false, "(absvector? \"foo\")");
        absvector[2] = 5;
        is(absvector, "(address-> (value v) 2 5)");
        is(5, "(<-address (value v) 2)");
        is(-1, "(trap-error (<-address (value v) 5) (lambda E -1))");
    }

    @Test
    public void eval_kl_freeze_and_thaw() {
        is(9, "(eval-kl (+ 4 5)");
        is(4, "(eval-kl 4)");
        is(intern("hello"), "(eval-kl hello)");
        is(intern("hello"), "(eval-kl hello)");
        is(MethodHandle.class, "(freeze (+ 2 2)");
        is(4, "((freeze (+ 2 2)) 0)");
    }

    @Test
    public void set_value_and_intern() {
        is(5, "(set x 5)");
        is(5, "(value x)");
        is(5, "(value (intern \"x\")");
    }

    @Test
    public void get_time() {
        is(Number.class, "(get-time run)");
    }

    @Test
    public void streams() {
        is(String.class, "(set fileName (cn (str (get-time run)) \".txt\"))");
        is(FileOutputStream.class, "(set writeFile (open file (value fileName) out))");
        is("foobar", "(pr \"foobar\" (value writeFile))");
        is(null, "(close (value writeFile))");
        is(FileInputStream.class, "(set readFile (open file (value fileName) in))");
        is(102, "(read-byte (value readFile))");
        is(111, "(read-byte (value readFile))");
        is(false, "(= 102 (read-byte (value readFile)))");
        is(null, "(close (value readFile))");
    }

    @After
    public void delete_file() {
        try {
            File file = new File((String) 神("(value *home-directory*)"), (String) 神("(value fileName)"));
            if (file.exists())
                assertTrue(file.delete());
        } catch (Exception ignore) {
        }
    }

    @Test
    public void n_gt_string() {
        is("d", "(n->string 100)");
        is("h", "(n->string 104)");
        is("(", "(n->string 40)");
        is(false, "(= \"d\" (n->string 101)");
        is(-1, "(trap-error (n->string -10) (lambda E -1)");
    }

    @Test
    public void string_gt_n() {
        is(100, "(string->n \"d\")");
        is(104, "(string->n \"h\")");
        is(40, "(string->n \"(\")");
        is(false, "(= 101 (string->n \"d\")");
        is(-1, "(trap-error (string-> \"\") (lambda E -1)");
    }

    @Test
    public void special_tests() {
        is(EMPTY_LIST, "()");
        is(-1, "(trap-error ((4 3 2)) (lambda E -1))");
        is(-1, "(trap-error (+4 2) (lambda E -1))");
        is(-1, "(trap-error (+ 4 \"2\") (lambda E -1))");
        is(-1, "(trap-error (+ 4 specialTest) (lambda E -1))");
        is(-1, "(trap-error (+ 4 true) (lambda E -1))");
    }

    @Test
    public void lists() {
        is(-1, "(trap-error (hd 5) (lambda E -1))");
        is(-1, "(trap-error (tl 5) (lambda E -1))");
        is(1, "(hd '(1 2 3)");
        is(asList(2, 3), "(tl '(1 2 3)");
        is(new Cons(5, 10), "(cons 5 10)");
    }

    @Test
    public void strings() {
        is("hello", "(str \"hello\")");
        is("5", "(str 5)");
        is(true, "(string? (str 5)");
        is(true, "(string? (str hello)");
        is(true, "(string? (str \"hello\")");
        is("helloWorld", "(cn \"hello\" \"World\")");
        is("hello", "(cn \"hello\" \"\")");
        is("hello", "(cn \"\" \"hello\")");
    }

    @Test
    public void number() {
        is(1000.0, "10e2");
        is(true, "(= 1.0 1)");
//        is(true, "(--3 3)");
//        is(true, "(---5 5.0)");
    }

    void is(Object expected, String actual) {
        if (expected instanceof Class)
            assertThat(神(actual), instanceOf((Class<?>) expected));
        else
            assertThat(神(actual), equalTo(expected));
    }

    Object 神(String shen) {
        try {
            return readEval(shen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
