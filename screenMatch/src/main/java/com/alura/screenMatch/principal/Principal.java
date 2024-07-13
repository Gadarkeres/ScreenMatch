package com.alura.screenMatch.principal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.alura.screenMatch.model.DadosEpisodio;
import com.alura.screenMatch.model.DadosSerie;
import com.alura.screenMatch.model.DadosTemporada;
import com.alura.screenMatch.service.ConsumoApi;
import com.alura.screenMatch.service.ConverterDados;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverterDados conversor = new ConverterDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=a1239a9c"; // final significa que o valor não pode ser alterado, igual
                                                       // const do
    // javascript

    public void exibeMenu() {
        System.out.println("Digite o nome da serie para iniciar a busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(' ', '+') + API_KEY); // e agora eu tenho o json com
                                                                                         // // a serie que eu quero
                                                                                         // consumir
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumo.obterDados(ENDERECO + nomeSerie.replace(' ', '+') + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json,
                    DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        temporadas.forEach(System.out::println); // :: é equivalente a (dados) -> System.out.println(dados).

        // for (int i = 0; i < dados.totalTemporadas(); i++) {
        //     List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
        //     for (int j = 0; j < episodiosTemporada.size(); j++) {
        //         System.out.println(episodiosTemporada.get(j).titulo()); 
        //     }
        // }

        temporadas.forEach(temporada -> {
            temporada.episodios().forEach(episodio -> { // java 
                System.out.println(episodio.titulo());
            });
        });

        // Temporada((parametro) -> expressao) 
        List<String> nomes = Arrays.asList("Matheus", "João", "Maria", "Pedro", "Joaquim");
        nomes.stream().sorted().limit(3).filter(name -> name.startsWith("M")).map(name -> name.toUpperCase())
                .forEach(System.out::println); // ação intermedia, tudo que gera um stream novo,
        // e operação final, é operação que usa a stream,
        // consumindo a stream.
    }

}
