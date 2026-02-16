package com.saull.juegomental;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@Controller
public class JuegoController {

    // ===== MODELO DE PREGUNTA =====
    static class Pregunta {
        String texto;
        String[] opciones;
        int correcta;

        Pregunta(String texto, String[] opciones, int correcta) {
            this.texto = texto;
            this.opciones = opciones;
            this.correcta = correcta;
        }
    }

    // ===== BANCO DE PREGUNTAS =====
    List<Pregunta> banco = List.of(
            new Pregunta("Un número más su mitad es 30. ¿Cuál es?",
                    new String[]{"10","15","20","25"}, 2),
            new Pregunta("El doble de un número es 18. ¿Cuál es?",
                    new String[]{"6","8","9","12"}, 2),
            new Pregunta("Tres números iguales suman 21. ¿Cada uno es?",
                    new String[]{"5","6","7","8"}, 2),
            new Pregunta("La mitad de un número más 3 es 11. ¿Cuál es?",
                    new String[]{"14","16","18","20"}, 1),
            new Pregunta("Un número menos su mitad es 10. ¿Cuál es?",
                    new String[]{"10","15","20","25"}, 2),
            new Pregunta("El triple de un número es 24. ¿Cuál es?",
                    new String[]{"6","7","8","9"}, 2),
            new Pregunta("Si x + 5 = 12, ¿x es?",
                    new String[]{"5","6","7","8"}, 2),
            new Pregunta("Si 2x = 14, ¿x es?",
                    new String[]{"6","7","8","9"}, 1),
            new Pregunta("Un número dividido entre 5 es 4. ¿Cuál es?",
                    new String[]{"15","20","25","30"}, 1),
            new Pregunta("Dos números iguales suman 40. ¿Cada uno es?",
                    new String[]{"10","15","20","25"}, 2),
            new Pregunta("Un número más 8 es 15. ¿Cuál es?",
                    new String[]{"5","6","7","8"}, 2),
            new Pregunta("El doble de 9 es:",
                    new String[]{"16","17","18","19"}, 2),
            new Pregunta("La mitad de 50 es:",
                    new String[]{"20","25","30","35"}, 1),
            new Pregunta("Si 3x = 30, ¿x es?",
                    new String[]{"5","8","10","12"}, 2),
            new Pregunta("Un número menos 7 es 9. ¿Cuál es?",
                    new String[]{"14","15","16","17"}, 2),
            new Pregunta("Si x/2 = 6, ¿x es?",
                    new String[]{"10","11","12","13"}, 2),
            new Pregunta("El doble de la mitad de 20 es:",
                    new String[]{"10","15","20","25"}, 2),
            new Pregunta("Si x + x = 18, ¿x es?",
                    new String[]{"6","7","8","9"}, 3),
            new Pregunta("Un número más 3 es igual a 3. ¿Cuál es?",
                    new String[]{"0","1","2","3"}, 0),
            new Pregunta("Si 4x = 16, ¿x es?",
                    new String[]{"2","3","4","5"}, 2)
    );

    // ===== INICIO =====
    @GetMapping("/")
    public String inicio(HttpSession session) {
        session.setAttribute("preguntaActual", 0);
        session.setAttribute("checkpoint", 0);
        return "inicio";
    }

    // ===== MOSTRAR PREGUNTA =====
    @GetMapping("/escenario1")
    public String pregunta(HttpSession session, Model model) {

        Integer indiceObj = (Integer) session.getAttribute("preguntaActual");

        if (indiceObj == null) {
            indiceObj = 0;
            session.setAttribute("preguntaActual", 0);
        }

        int indice = indiceObj;

        if (indice >= banco.size()) {
            return "victoria";
        }

        Pregunta p = banco.get(indice);

        session.setAttribute("respuestaCorrecta", p.correcta);

        model.addAttribute("pregunta", p.texto);
        model.addAttribute("opciones", p.opciones);
        model.addAttribute("numero", indice + 1);
        model.addAttribute("progreso", (indice * 100) / banco.size());

        return "escenario1";
    }

    // ===== RESPONDER =====
    @PostMapping("/responder")
    public String responder(@RequestParam int opcion, HttpSession session) {

        int correcta = (int) session.getAttribute("respuestaCorrecta");
        int indice = (int) session.getAttribute("preguntaActual");
        int checkpoint = (int) session.getAttribute("checkpoint");

        if (opcion == correcta) {

            indice++;
            session.setAttribute("preguntaActual", indice);

            // ⭐ Guardar checkpoint SOLO después de 5 correctas
            if (indice >= 5 && indice % 5 == 0) {
                session.setAttribute("checkpoint", indice);
            }

            return "redirect:/animacion/escenario2";

        } else {

            // ❌ Si aún no hay checkpoint → volver al inicio
            if (checkpoint < 5) {
                session.setAttribute("preguntaActual", 0);
            }
            // ❌ Si ya hay checkpoint → volver al checkpoint
            else {
                session.setAttribute("preguntaActual", checkpoint);
            }

            return "redirect:/animacion/escenario3";
        }
    }

    // ===== ANIMACIÓN =====
    @GetMapping("/animacion/{destino}")
    public String animacion(@PathVariable String destino, Model model) {
        model.addAttribute("destino", "/" + destino);
        return "animacion";
    }

    // ===== ESCENARIO CORRECTO =====
    @GetMapping("/escenario2")
    public String bien() {
        return "escenario2";
    }

    // ===== ESCENARIO INCORRECTO =====
    @GetMapping("/escenario3")
    public String mal() {
        return "escenario3";
    }
}
