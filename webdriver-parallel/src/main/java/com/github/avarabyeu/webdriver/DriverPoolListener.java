package com.github.avarabyeu.webdriver;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.lang.reflect.Field;

/**
 * @author Andrei Varabyeu
 */
public class DriverPoolListener extends TestListenerAdapter {

    @Override
    public void onTestSuccess(ITestResult tr) {
        quitDriver(tr);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        quitDriver(tr);
    }

    private void quitDriver(ITestResult tr) {
        for (Field f : tr.getInstance().getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(QuitAfterTest.class)) {
                try {
                    ((WebDriver) f.get(tr.getInstance())).quit();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
