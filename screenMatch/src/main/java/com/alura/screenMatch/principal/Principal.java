package com.alura.screenMatch.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.alura.screenMatch.model.DadosEpisodio;
import com.alura.screenMatch.model.DadosSerie;
import com.alura.screenMatch.model.DadosTemporada;
import com.alura.screenMatch.model.Episodio;
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

        temporadas.forEach(temporada -> {
            temporada.episodios().forEach(episodio -> { // java 
                System.out.println(episodio.titulo());
            });
        });
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
        .flatMap(t -> t.episodios().stream()).collect(Collectors.toList());

            System.out.println("\n Top 5 episodios: ");

            dadosEpisodios.stream().sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed()).limit(5).filter(episodio -> !episodio.avaliacao().equalsIgnoreCase("N/A")).forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream().flatMap(t -> t.episodios().stream().map(d -> new Episodio(d.numero(), d))).collect(Collectors.toList());

            episodios.forEach(System.out::println);

             //
            System.out.println("A partir de qual ano voce quer saber os episodios? ");
            var ano = leitura.nextInt();
            leitura.nextLine();

            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dataBusca = LocalDate.of(ano, 1, 1);
            episodios.stream().filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca)).forEach(e -> System.out.println(
            "Temporada:  " + e.getTemporada() + "Episodio:  " + e.getNumeroEpisodio() + "Data de lançamento:   " + e.getDataLancamento().format(formatador)
            
            ));


    }

}
