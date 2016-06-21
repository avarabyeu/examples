package com.github.avarabyeu.webdriver;

import com.google.common.base.Preconditions;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.internal.WrapsElement;

import javax.inject.Provider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Provider contains pool of {@link WebDriver} objects.
 * Each returned {@link WebDriver} is a proxy class implementing {@link WrapsDriver} interface
 * Calling {@link WebDriver#quit()} returns WebDriver to the pool
 *
 * @author Andrei Varabyeu
 */
public class DriverPoolProvider implements Provider<WebDriver> {
    private GenericObjectPool<WebDriver> pool;

    /**
     * @param delegate WebDriver's provider.
     *                 Creation of new WebDriver in the pool is delegated to this provider
     * @param poolSize Size of the pool
     */
    public DriverPoolProvider(Provider<WebDriver> delegate, int poolSize) {

        Preconditions.checkNotNull(delegate, "Provider delegate shouldn't be null");
        Preconditions.checkArgument(poolSize > 0, "%s is incorrect pool size. Should be more than zero", poolSize);

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(poolSize);

        this.pool = new GenericObjectPool<>(new BasePooledObjectFactory<WebDriver>() {

            @Override
            public WebDriver create() throws Exception {
                return delegate.get();
            }

            @Override
            public PooledObject<WebDriver> wrap(WebDriver value) {
                return new DefaultPooledObject<>(value);
            }

            @Override
            public void destroyObject(PooledObject<WebDriver> p) throws Exception {
                System.out.println("destroying!");
                p.getObject().quit();
                super.destroyObject(p);
            }

        }, config);
    }

    /**
     * Borrow WebDriver from the pool or create new one
     * @see {@link ObjectPool#borrowObject()}
     *
     * @return WebDriver instance from the pool
     */
    @Override
    public WebDriver get() {
        try {
            WebDriver wd = pool.borrowObject();
            return (WebDriver) Proxy
                    .newProxyInstance(this.getClass().getClassLoader(),
                            extractInterfaces(wd),
                            new PooledDriverHandler(wd));

        } catch (Exception e) {
            throw new RuntimeException("Unable to obtain WebDriver from the pool", e);
        }
    }

    public void shutdown() {
        try {
            AbandonedConfig abandonedConfig = new AbandonedConfig();
            abandonedConfig.setRemoveAbandonedOnMaintenance(true);
            abandonedConfig.setRemoveAbandonedTimeout(0);
            pool.setAbandonedConfig(abandonedConfig);

            pool.evict();
            pool.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to kill all opened instances");
        }
    }

    /**
     * @see {@link org.openqa.selenium.support.events.EventFiringWebDriver#extractInterfaces(Object)}
     */
    private Class<?>[] extractInterfaces(Object object) {
        Set<Class<?>> allInterfaces = new HashSet<>();
        allInterfaces.add(WrapsDriver.class);
        if (object instanceof WebElement) {
            allInterfaces.add(WrapsElement.class);
        }
        extractInterfaces(allInterfaces, object.getClass());

        return allInterfaces.toArray(new Class<?>[allInterfaces.size()]);
    }

    /**
     * @see {@link org.openqa.selenium.support.events.EventFiringWebDriver#extractInterfaces(Set, Class)}
     */
    private void extractInterfaces(Set<Class<?>> addTo, Class<?> clazz) {
        if (Object.class.equals(clazz)) {
            return; // Done
        }

        Class<?>[] classes = clazz.getInterfaces();
        addTo.addAll(Arrays.asList(classes));
        extractInterfaces(addTo, clazz.getSuperclass());
    }

    /**
     * Invocation handler. On 'quit' method returns WebDriver back to the pool
     */
    private class PooledDriverHandler implements InvocationHandler {

        private WebDriver delegate;

        PooledDriverHandler(WebDriver delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (noArgs(args) && "getWrappedDriver".equals(method.getName())) {
                return delegate;
            }

            if (noArgs(args) && "quit".equals(method.getName())) {
                pool.returnObject(delegate);
                return null;
            }

            /* notify pool that this instance in use */
            pool.use(delegate);
            return method.invoke(delegate, args);

        }

        private boolean noArgs(Object[] args) {
            return null == args || (0 == args.length);
        }
    }

}
