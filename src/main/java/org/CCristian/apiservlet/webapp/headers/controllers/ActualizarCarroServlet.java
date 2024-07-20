package org.CCristian.apiservlet.webapp.headers.controllers;

import org.CCristian.apiservlet.webapp.headers.models.Carro;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@WebServlet("/actualizarCarro")
public class ActualizarCarroServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Carro carro = (Carro) req.getSession().getAttribute("carro");

        if (carro != null) {
            Enumeration<String> parametros = req.getParameterNames();

            while (parametros.hasMoreElements()) {
                String param = parametros.nextElement();
                if (param.startsWith("cantidad_")) {
                    int id = Integer.parseInt(param.substring(9));
                    int cantidad = Integer.parseInt(req.getParameter(param));
                    carro.actualizarProducto(id, cantidad);
                }
            }

            String[] deleteIds = req.getParameterValues("deleteProductos");
            if (deleteIds != null) {
                for (String deleteId : deleteIds) {
                    int id = Integer.parseInt(deleteId);
                    carro.eliminarProducto(id);
                }
            }
        }

        resp.sendRedirect(req.getContextPath() + "/carro.jsp");
    }
}
