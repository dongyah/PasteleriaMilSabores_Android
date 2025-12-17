import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.time.Duration;
import java.util.List;

public class PruebasProductos {

    private AndroidDriver driver;
    private String appPackage = "com.example.pasteleriamilsabores";

    @BeforeClass
    public void setUp() {
        System.out.println("1. INICIANDO CONFIGURACIÓN DEL TEST...");

        try {
            UiAutomator2Options options = new UiAutomator2Options()
                    .setPlatformName("Android")
                    .setDeviceName("Android Emulator")
                    .setAutomationName("UiAutomator2")
                    // IMPORTANTE: Verifica que esta ruta a tu APK sea la correcta en tu PC
                    .setApp("C:/Users/belen/AndroidStudioProjects/PasteleriaMilSabores_Android/app/build/outputs/apk/debug/app-debug.apk")
                    .setNewCommandTimeout(Duration.ofSeconds(360))
                    .setNoReset(true); // No borra datos, útil para mantener sesión si ya entraste

            System.out.println("2. CONECTANDO CON APPIUM...");
            driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
            System.out.println("3. CONEXIÓN EXITOSA.");
            Thread.sleep(3000);

        } catch (Exception e) {
            System.err.println("ERROR FATAL EN SETUP: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void testCrearProducto() throws InterruptedException {
        System.out.println("--- EJECUTANDO TEST: CREAR PRODUCTO ---");

        // PASO 1: LOGIN (Usamos el login manual admin/123 que es más estable para pruebas)
        try {
            List<WebElement> camposUsuario = driver.findElements(AppiumBy.id(appPackage + ":id/etUsername"));

            if (!camposUsuario.isEmpty()) {
                System.out.println("Pantalla de Login detectada. Ingresando credenciales...");

                camposUsuario.get(0).sendKeys("admin");
                driver.findElement(AppiumBy.id(appPackage + ":id/etPassword")).sendKeys("123");

                try { driver.hideKeyboard(); } catch (Exception ignored) {}

                driver.findElement(AppiumBy.id(appPackage + ":id/btnLogin")).click();

                System.out.println("Login enviado.");
                Thread.sleep(3000);
            } else {
                System.out.println("No veo campos de login, asumo que ya estamos dentro.");
            }
        } catch (Exception e) {
            System.out.println("Error en bloque de Login: " + e.getMessage());
        }

        // PASO 2: NAVEGAR AL FORMULARIO (CORREGIDO CON TU XML)
        try {
            // TU XML DICE: android:id="@+id/ivAddProduct"
            WebElement btnAgregar = driver.findElement(AppiumBy.id(appPackage + ":id/ivAddProduct"));
            btnAgregar.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("ERROR: No encuentro el botón 'ivAddProduct'. ¿El Login funcionó?");
            return;
        }


        System.out.println("Llenando formulario de producto...");

        try {
            driver.findElement(AppiumBy.id(appPackage + ":id/etProductCode")).sendKeys("TEST-" + System.currentTimeMillis());
            driver.findElement(AppiumBy.id(appPackage + ":id/etProductName")).sendKeys("Torta Test Appium");

            try { driver.hideKeyboard(); } catch (Exception ignored) {}

            driver.findElement(AppiumBy.id(appPackage + ":id/etProductPrice")).sendKeys("15000");
            driver.findElement(AppiumBy.id(appPackage + ":id/etProductStock")).sendKeys("10");
            driver.findElement(AppiumBy.id(appPackage + ":id/etCriticalStock")).sendKeys("2");

            try { driver.hideKeyboard(); } catch (Exception ignored) {}

            driver.findElement(AppiumBy.id(appPackage + ":id/spinnerCategory")).click();
            Thread.sleep(1000);
            try {
                driver.findElement(AppiumBy.xpath("//android.widget.ListView/android.widget.TextView[1]")).click();
            } catch (Exception ex) {
                driver.findElement(AppiumBy.className("android.widget.CheckedTextView")).click();
            }
            Thread.sleep(1000);

            System.out.println("🔵 Haciendo scroll para guardar...");
            WebElement btnGuardar = driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true))" +
                            ".scrollIntoView(new UiSelector().resourceId(\"" + appPackage + ":id/btnSaveProduct\"))"));

            btnGuardar.click();
            System.out.println("✅ Botón Guardar presionado.");

            Thread.sleep(4000); // Esperar a que vuelva al inicio

            if(driver.findElement(AppiumBy.id(appPackage + ":id/ivLogout")).isDisplayed()){
                System.out.println(" EXITO: Producto creado y regresamos al Dashboard.");
            }

        } catch (Exception e) {
            System.err.println("Error llenando el formulario: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    public void testEliminarProducto() throws InterruptedException {
        System.out.println("--- EJECUTANDO TEST: ELIMINAR PRODUCTO ---");

        // CORREGIDO CON TU XML: item_product.xml usa "@+id/btnDelete"
        List<WebElement> botonesEliminar = driver.findElements(AppiumBy.id(appPackage + ":id/btnDelete"));

        if (botonesEliminar.isEmpty()) {
            System.out.println(" No hay productos para eliminar o el ID 'btnDelete' no coincide.");
            return;
        }

        System.out.println("🔵 Eliminando el primer producto encontrado...");
        botonesEliminar.get(0).click();
        Thread.sleep(1000);

        try {
            driver.findElement(AppiumBy.id("android:id/button1")).click();
            System.out.println("Confirmación aceptada.");
        } catch (Exception e) {
            System.out.println("No apareció diálogo de confirmación, se borró directo o ID distinto.");
        }

        Thread.sleep(2000);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("🏁 Test finalizado. Driver cerrado.");
        }
    }
}