package com.github.avarabyeu.test;

import com.github.avarabyeu.webdriver.DriverPoolProvider;
import com.google.inject.Provider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.Test;

/**
 * Created by avarabyeu on 6/8/16.
 */
public class ParallelTest {

    private static DriverPoolProvider provider = new DriverPoolProvider(new Provider<WebDriver>() {
        @Override
        public WebDriver get() {
            return new FirefoxDriver();
        }
    }, 5);


    @Test
    public void someTest(){
        WebDriver webDriver = provider.get();
        webDriver.get("http://tut.by");
//        webDriver.quit();
    }

}
