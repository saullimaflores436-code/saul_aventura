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

    // ===== BANCO DE PREGUNTAS (FÁCIL CON TRAMPA → DIFÍCIL) =====
    List<Pregunta> banco = List.of(

            // 🟢 NIVEL FÁCIL (PARECEN SIMPLES, PERO TIENEN TRAMPA)

            new Pregunta("Si tienes 3 manzanas y tomas 2, ¿cuántas tienes?",
                    new String[]{"1","2","3","5"}, 1), // tienes 2 porque las tomaste

            new Pregunta("El doble de 5 más 3 es:",
                    new String[]{"13","16","10","8"}, 0),
            // trampa: (5*2)+3 = 13, no 16

            new Pregunta("¿Qué es mayor: 2x cuando x=3 o 3x cuando x=2?",
                    new String[]{"2x","3x","Son iguales","No se puede saber"}, 2),
            // 2*3 = 6 y 3*2 = 6

            new Pregunta("La mitad de 8 más 4 es:",
                    new String[]{"6","8","4","12"}, 1),
            // trampa: (8/2)+4 = 8

            new Pregunta("Si un número es 0, ¿cuánto es su doble?",
                    new String[]{"0","1","No existe","Depende"}, 0),


            // 🟡 NIVEL MEDIO (ÁLGEBRA DIRECTA)

            new Pregunta("Si 2x + 4 = 14, ¿x es?",
                    new String[]{"4","5","6","7"}, 1),

            new Pregunta("Si x/3 = 5, ¿x es?",
                    new String[]{"10","12","15","18"}, 2),

            new Pregunta("Un número más su mitad es 18. ¿Cuál es?",
                    new String[]{"10","12","14","16"}, 1),

            new Pregunta("Si 3x - 6 = 9, ¿x es?",
                    new String[]{"3","4","5","6"}, 2),

            new Pregunta("Dos números iguales suman 50. ¿Cada uno es?",
                    new String[]{"20","25","30","15"}, 1),


            // 🔴 NIVEL DIFÍCIL (VARIOS PASOS)

            new Pregunta("Un número más su doble menos 4 es 20. ¿Cuál es?",
                    new String[]{"6","7","8","9"}, 2),
            // x + 2x - 4 = 20 → 3x = 24 → x=8

            new Pregunta("La mitad de un número más su triple es 35. ¿Cuál es?",
                    new String[]{"8","9","10","12"}, 2),
            // x/2 + 3x = 35 → 7x/2=35 → x=10

            new Pregunta("Si 4(x - 2) = 24, ¿x es?",
                    new String[]{"6","7","8","9"}, 2),

            new Pregunta("Un número menos 5 dividido entre 3 es 5. ¿Cuál es?",
                    new String[]{"18","20","21","24"}, 1),
            // (x-5)/3 = 5 → x-5=15 → x=20

            new Pregunta("Si x + (x/2) + (x/4) = 21, ¿x es?",
                    new String[]{"8","10","12","14"}, 2)
            // fracciones combinadas

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
