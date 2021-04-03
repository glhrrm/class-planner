package br.edu.ifrs.classplanner;

import org.junit.Assert;
import org.junit.Test;

import br.edu.ifrs.classplanner.helper.Helper;

/*
Testa os métodos da classe Helper
 */
public class HelperUnitTest {

    /*
    Data é válida e está no formato correto. Logo, o teste deve retornar verdadeiro.
     */
    @Test
    public void dateMustBeValid() {
        boolean result = Helper.isValidDateOrTime("01/05/2021", Helper.DATE);
        Assert.assertTrue(result);
    }

    /*
    Apesar de estar no formato correto, a data é inválida. Logo, o teste deve retornar falso.
     */
    @Test
    public void dateMustNotBeValid() {
        boolean result = Helper.isValidDateOrTime("31/04/2021", Helper.DATE);
        Assert.assertFalse(result);
    }

    /*
    Apesar de a data ser um feriado, está no formato incorreto (deveria ser DD/MM/YYYY).
    Logo, o teste deve retornar falso.
     */
    @Test
    public void mustNotBeAHoliday_wrongFormat() {
        boolean result = Helper.isHoliday("2021/05/01");
        Assert.assertFalse(result);
    }

}
