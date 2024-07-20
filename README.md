<h1 align="center">Tarea: Actualizar y eliminar ítems del carro de compras</h1>
<p>Actualizar y eliminar ítems del carro de compras</p>
<h2>Instrucciones de tareas</h2>
<p>Como funcionalidad extra del carro de compras se pide que se pueda actualizar la cantidad y eliminar los ítems de la siguiente manera:</p>
<p>Actualizar la cantidad mediante un campo cantidad en cada linea del carro y eliminar mediante checkboxes seleccionables por cada ítems del carro.</p>
<p>Para eso se necesita de:</p>

- un formulario anidado en la vista carro.jsp
- un nuevo servlet llamado ActualizarCarroServlet para actualizar los ítems del carro
- un par de métodos en la clase Carro para eliminar y actualizar productos.

<p>Una vez terminada y probada la tarea deberán publicar el código fuente de todos los archivos involucrados, además de las imágenes screenshot (imprimir pantalla).</p>

- Vista carro.jsp.

- Clase servlet ActualizarCarroServlet.

- Clase Carro.

- Imágenes screenshot del programa funcionando en el navegador.

<h1>Resolución del Profesor</h1>

- La vista carro.jsp (actualizada)

```jsp
<%@page contentType="text/html" pageEncoding="UTF-8" import="org.aguzman.apiservlet.webapp.headers.models.*"%>
<%
Carro carro = (Carro) session.getAttribute("carro");
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Carro de Compras</title>
    </head>
    <body>
        <h1>Carro de Compras</h1>
        <% if(carro == null || carro.getItems().isEmpty()){%>
        <p>Lo sentimos no hay productos en el carro de compras!</p>
        <%} else { %>
        <form name="formcarro" action="<%=request.getContextPath()%>/actualizar-carro" method="post">
            <table>
                <tr>
                    <th>id</th>
                    <th>nombre</th>
                    <th>precio</th>
                    <th>cantidad</th>
                    <th>total</th>
                    <th>borrar</th>
                </tr>
                <%for(ItemCarro item: carro.getItems()){%>
                <tr>
                    <td><%=item.getProducto().getId()%></td>
                    <td><%=item.getProducto().getNombre()%></td>
                    <td><%=item.getProducto().getPrecio()%></td>
                    <td><input type="text" size="4" name="cant_<%=item.getProducto().getId()%>" value="<%=item.getCantidad()%>" /></td>
                    <td><%=item.getImporte()%></td>
                    <td><input type="checkbox" value="<%=item.getProducto().getId()%>" name="deleteProductos" /></td>
                </tr>
                <%}%>
                <tr>
                    <td colspan="4" style="text-align: right">Total:</td>
                    <td><%=carro.getTotal()%></td>
                </tr>
            </table>
            <a href="javascript:document.formcarro.submit();">Actualizar</a>
        </form>
        <%}%>
        <p><a href="<%=request.getContextPath()%>/productos">seguir comprando</a></p>
        <p><a href="<%=request.getContextPath()%>/index.html">volver</a></p>
    </body>
</html>
```

- La clase ActualizarCarroServlet (nueva)

```java
package org.aguzman.apiservlet.webapp.headers.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.aguzman.apiservlet.webapp.headers.models.Carro;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@WebServlet("/actualizar-carro")
public class ActualizarCarroServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("carro") != null) {
            Carro carro = (Carro) session.getAttribute("carro");
            updateProductos(req, carro);
            updateCantidades(req, carro);
        }
        resp.sendRedirect(req.getContextPath() + "/ver-carro");
    }

    private void updateProductos(HttpServletRequest request, Carro carro) {
        String[] deleteIds = request.getParameterValues("deleteProductos");

        if (deleteIds != null && deleteIds.length > 0) {
            List<String> productoIds = Arrays.asList(deleteIds);
            // Borramos los productos del carrito.
            carro.removeProductos(productoIds);
        }

    }

    private void updateCantidades(HttpServletRequest request, Carro carro) {

        Enumeration<String> enumer = request.getParameterNames();

        // Iteramos a traves de los parámetros y buscamos los que empiezan con
        // "cant_". El campo cant en la vista fueron nombrados "cant_" + productoId.
        // Obtenemos el id de cada producto y su correspondiente cantidad ;-).
        while (enumer.hasMoreElements()) {
            String paramName = enumer.nextElement();
            if (paramName.startsWith("cant_")) {
                String id = paramName.substring(5);
                String cantidad = request.getParameter(paramName);
                if (cantidad != null) {
                    carro.updateCantidad(id, Integer.parseInt(cantidad));
                }
            }
        }
    }
}
```

- La clase Carro (actualizada)

```java
package org.aguzman.apiservlet.webapp.headers.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Carro {
    private List<ItemCarro> items;

    public Carro() {
        this.items = new ArrayList<>();
    }

    public void addItemCarro(ItemCarro itemCarro) {
        if (items.contains(itemCarro)) {
            Optional<ItemCarro> optionalItemCarro = items.stream()
                    .filter(i -> i.equals(itemCarro))
                    .findAny();
            if (optionalItemCarro.isPresent()) {
                ItemCarro i = optionalItemCarro.get();
                i.setCantidad(i.getCantidad()+1);
            }
        } else {
            this.items.add(itemCarro);
        }
    }
    public List<ItemCarro> getItems() {
        return items;
    }

    public int getTotal() {
        return items.stream().mapToInt(ItemCarro::getImporte).sum();
    }

    public void removeProductos(List<String> productoIds) {
        if (productoIds != null) {
            productoIds.forEach(this::removeProducto);
            // que es lo mismo a:
            // productoIds.forEach(productoId -> removeProducto(productoId));
        }
    }

    public void removeProducto(String productoId) {
        Optional<ItemCarro> producto = findProducto(productoId);
        producto.ifPresent(itemCarro -> items.remove(itemCarro));
    }

    public void updateCantidad(String productoId, int cantidad) {
        Optional<ItemCarro> producto = findProducto(productoId);
        producto.ifPresent(itemCarro -> itemCarro.setCantidad(cantidad));
    }

    private Optional<ItemCarro> findProducto(String productoId) {
        return  items.stream()
                .filter(itemCarro -> productoId.equals(Long.toString(itemCarro.getProducto().getId())))
                .findAny();
    }
}
```
