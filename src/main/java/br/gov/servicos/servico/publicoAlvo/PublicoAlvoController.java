package br.gov.servicos.servico.publicoAlvo;

import br.gov.servicos.busca.Buscador;
import br.gov.servicos.cms.Conteudo;
import br.gov.servicos.servico.Servico;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Controller
@FieldDefaults(level = PRIVATE, makeFinal = true)
class PublicoAlvoController {

    Buscador buscador;

    @Autowired
    PublicoAlvoController(Buscador buscador) {
        this.buscador = buscador;
    }

    @RequestMapping("/publico-alvo/{id}")
    ModelAndView publicoAlvo(@PathVariable String id,
                             @RequestParam(required = false) Character letra) {

        Character primeiraLetra = ofNullable(letra).map(Character::toUpperCase).orElse('A');
        Map<Character, List<Servico>> servicosPorLetraInicial = servicosAgrupadosPorLetraInicial(id);

        List<Conteudo> servicos = servicosPorLetraInicial.getOrDefault(primeiraLetra, Collections.<Servico>emptyList())
                .stream()
                .sorted((x, y) -> x.getId().compareTo(y.getId()))
                .map(Conteudo::fromServico)
                .collect(toList());

        Map<String, Object> model = new HashMap<>();
        model.put("letraAtiva", primeiraLetra);
        model.put("publicoAlvo", extraiPublicoAlvo(id, servicosPorLetraInicial.get(primeiraLetra)));
        model.put("servicos", servicos);
        model.put("letras", letrasDisponiveis(servicosPorLetraInicial.keySet()));

        return new ModelAndView("publico-alvo", model);
    }

    private Map<Character, List<Servico>> servicosAgrupadosPorLetraInicial(String publicoAlvo) {
        return buscador.buscaServicosPor("publicosAlvo.id", ofNullable(publicoAlvo))
                .stream()
                .collect(groupingBy(s -> s.getTitulo().trim().toUpperCase().charAt(0)));
    }

    private List<Character> letrasDisponiveis(Set<Character> letras) {
        return letras
                .stream()
                .sorted()
                .collect(toList());
    }

    private PublicoAlvo extraiPublicoAlvo(String id, List<Servico> servicos) {
        return servicos
                .stream()
                .flatMap(s -> s.getPublicosAlvo().stream())
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .get();
    }

}
