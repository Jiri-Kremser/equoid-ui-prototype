package io.radanalytics.equoid.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.radanalytics.equoid.config.ApplicationProperties;
import io.radanalytics.equoid.domain.Item;

import io.radanalytics.equoid.repository.ItemRepository;
import io.radanalytics.equoid.web.rest.errors.BadRequestAlertException;
import io.radanalytics.equoid.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing Item.
 */
@RestController
@RequestMapping("/api")
public class ItemResource {

    private final Logger log = LoggerFactory.getLogger(ItemResource.class);

    private static final String ENTITY_NAME = "item";

    private final ItemRepository itemRepository;

    private final ItemJdgManager jdgManager;

    private final ApplicationProperties props;

    public ItemResource(ItemRepository itemRepository, ItemJdgManager jdgManager, ApplicationProperties props) {
        this.itemRepository = itemRepository;
        this.jdgManager = jdgManager;
        this.props = props;
    }

    /**
     * POST  /items : Create a new item.
     *
     * @param item the item to create
     * @return the ResponseEntity with status 201 (Created) and with body the new item, or with status 400 (Bad Request) if the item has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/items")
    @Timed
    public ResponseEntity<String> createItem(@Valid @RequestBody Item item) throws URISyntaxException {
        log.info("REST request to save Item : {}", item);
        if (item.getId() != null) {
            throw new BadRequestAlertException("A new item cannot already have an ID", ENTITY_NAME, "idexists");
        }
//        jdgManager.put(item);

        RestTemplate restTemplate = new RestTemplate();
        log.debug("Adding new frequent item: " + item);
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity("http://" + props.getPublisher() + "/api", item.getName(), String.class);
        if (!stringResponseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("Error: " + stringResponseEntity.toString());
            return stringResponseEntity;
        }

        Item result = itemRepository.save(item);
        return ResponseEntity.created(new URI("/api/items/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(item.getName());
    }

    /**
     * PUT  /items : Updates an existing item.
     *
     * @param item the item to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated item,
     * or with status 400 (Bad Request) if the item is not valid,
     * or with status 500 (Internal Server Error) if the item couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/items")
    @Timed
    public ResponseEntity<String> updateItem(@Valid @RequestBody Item item) throws URISyntaxException {
        log.debug("REST request to update Item : {}", item);
        if (item.getId() == null) {
            return createItem(item);
        }
        jdgManager.put(item);
        Item result = itemRepository.save(item);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, item.getId().toString()))
            .body(result.getName());
    }

    /**
     * GET  /items : get all the items.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of items in body
     */
    @GetMapping("/items")
    @Timed
    public Map<String, List<Item>> getAllItems(@RequestParam(required = false, defaultValue = "false") boolean cached) {
        log.debug("REST request to get all Items");
        if (cached) {
            return jdgManager.getAll();
        } else {
            return new HashMap<String, List<Item>>(){
                {
                    put("all", itemRepository.findAll());
                }
            };
        }
    }

    /**
     * GET  /items/:id : get the "id" item.
     *
     * @param id the id of the item to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the item, or with status 404 (Not Found)
     */
    @GetMapping("/items/{id}")
    @Timed
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        log.debug("REST request to get Item : {}", id);
        Item item = itemRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(item));
    }

    /**
     * DELETE  /items/:id : delete the "id" item.
     *
     * @param id the id of the item to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/items/{id}")
    @Timed
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        log.debug("REST request to delete Item : {}", id);
        itemRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
