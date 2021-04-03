package br.edu.ifrs.classplanner;

import org.junit.Assert;
import org.junit.Test;

import br.edu.ifrs.classplanner.helper.Helper;

public class HelperUnitTest {

    @Test
    public void dateMustBeValid() {
        boolean result = Helper.isValidDateOrTime("01/05/2021", Helper.DATE);
        Assert.assertTrue(result);
    }

    @Test
    public void dateMustNotBeValid() {
        boolean result = Helper.isValidDateOrTime("31/04/2021", Helper.DATE);
        Assert.assertFalse(result);
    }

    @Test
    public void mustNotBeAHoliday_wrongFormat() {
        boolean result = Helper.isHoliday("2021/05/01");
        Assert.assertFalse(result);
    }

}
