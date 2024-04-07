package com.company.shoppingbotv2.service;

import com.company.shoppingbotv2.entity.User;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.company.shoppingbotv2.entity.enums.Language;
import com.company.shoppingbotv2.exception.ApiNotWorkingException;
import com.company.shoppingbotv2.payload.*;
import com.company.shoppingbotv2.utils.AppConstants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiClient {
    @Value("${app.api.url}")
    private String baseUrl;
    private final RestTemplate restTemplate;
    @Value("${app.api.username}")
    private String username;
    @Value("${app.api.password}")
    private String password;
    private String apiKey;

    @PostConstruct
    private void init() {
        // Concatenate username and password with a colon
        String credentials = this.username + ":" + this.password;
        // Encode credentials in Base64
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        // Add "Basic " prefix to the Base64 encoded credentials
        this.apiKey = AppConstants.API_KEY_PREFIX + encodedCredentials;
    }

    public String getComment(Language language) {
//        return "Comment";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("type", "comment")
                    .queryParam("lang", language.getApiCode());

            ResponseEntity<Map> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            return ((Map<String, String>) responseEntity.getBody()).get("comment");
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Comment olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "comment",
                                    "language", language
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public Map<String, String> getCustomerName(Language language, String phoneNumber, long chatId) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("type", "phone")
                    .queryParam("lang", language.getApiCode())
                    .queryParam("chat_id", chatId)
                    .queryParam("phone_number", phoneNumber);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
            return responseBody;
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Mijoz ismini olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "phone",
                                    "language", language,
                                    "phoneNumber", phoneNumber,
                                    "chatId", chatId
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public DebtCheckResponse getDebtContracts(long chatId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("type", "debt")
                    .queryParam("chat_id", chatId);

            ResponseEntity<DebtCheckResponse> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), DebtCheckResponse.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Contractlarni olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "debt",
                                    "chatId", chatId
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public ContractResponse getContractById(long chatId, String contractId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("type", "debt")
                    .queryParam("chat_id", chatId)
                    .queryParam("contract_id", contractId);

            ResponseEntity<ContractResponse> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), ContractResponse.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Contractni olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "debt",
                                    "chatId", chatId,
                                    "contractId", contractId
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public String getContact() {
//        return "+998335940230";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("type", "contact");

            ResponseEntity<Map> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            return ((Map<String, String>) responseEntity.getBody()).get("contact");
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Comment olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "contact"
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public String downloadActDoc(Long telegramId, String contractRemoteId, String startDate, String finishDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .encode(StandardCharsets.ISO_8859_1)
                .queryParam("type", "act")
                .queryParam("chat_id", telegramId);
        if (!Objects.isNull(contractRemoteId)) builder.queryParam("contract_id", contractRemoteId);
        if (!Objects.isNull(startDate)) builder.queryParam("start", startDate);
        if (!Objects.isNull(finishDate)) builder.queryParam("finish", finishDate);

        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
        );

        String fileName = generateUniqueFileName();

        // Save the file to resources/files directory
        String destinationPath = "src/main/resources/files/" + fileName;
        saveToFile(responseEntity.getBody(), destinationPath);

        return destinationPath;
    }

    private String generateUniqueFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = dateFormat.format(new Date());
        return "АктСверка_" + timestamp + ".xlsx";
    }

    private void saveToFile(byte[] content, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CategoryList getCategories(Long chatId, String userTIN) {
        try {
                HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("chat_id", chatId)
                    .queryParam("tin", userTIN)
                    .queryParam("type", "categories");



            ResponseEntity<CategoryList> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    CategoryList.class
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Categorylarni olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "categories",
                                    "chatId", chatId
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public ProductList showProductList(Long chatId, String categoryId, int page) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .encode(StandardCharsets.ISO_8859_1)
                    .queryParam("type", "category")
                    .queryParam("chat_id", chatId)
                    .queryParam("category_id", categoryId)
                    .queryParam("page", page)
                    .queryParam("page_size", AppConstants.PAGE_SIZE);
            ResponseEntity<ProductList> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    ProductList.class
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Productlarni category buiycha olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "category",
                                    "chatId", chatId,
                                    "categroy_id", categoryId,
                                    "page", page,
                                    "page_size", AppConstants.PAGE_SIZE
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public GetProduct getProduct(Long chatId, String productId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .encode(StandardCharsets.ISO_8859_1)
                    .queryParam("type", "numenclatury")
                    .queryParam("chat_id", chatId)
                    .queryParam("numenclatury_id", productId);

            ResponseEntity<ProductListResponse> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    ProductListResponse.class
            );
            ProductListResponse response = responseEntity.getBody();
            List<GetProduct> products = response != null ? response.products() : List.of();
            if (products != null && !products.isEmpty()) {
                return products.get(0);
            }
            throw new ApiNotWorkingException("Bitta productni idsi bo'yicha olganda shu product kelmadi", response);
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Productlarni olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "numenclatury",
                                    "chatId", chatId,
                                    "product_id", productId

                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public ProductListResponse searchProduct(Long chatId, String text, int page) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .encode(StandardCharsets.ISO_8859_1)
                    .queryParam("type", "numenclatury")
                    .queryParam("chat_id", chatId)
                    .queryParam("numenclatury_name", text)
                    .queryParam("page", page)
                    .queryParam("page_size", AppConstants.PAGE_SIZE);

            ResponseEntity<ProductListResponse> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    ProductListResponse.class
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Productlarni qidirishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "numenclatury",
                                    "chatId", chatId,
                                    "numenclatury_name", text,
                                    "page", page,
                                    "page_size", AppConstants.PAGE_SIZE
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public OrderDocument orderByUser(Long chatId, String userTIN, List<OrderItem> orderItemList) {
        String orderItemString = orderItemListToJsonStringMapper(orderItemList);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the request body object
            CreateOrderRequest requestObject = new CreateOrderRequest(chatId, orderItemList);

            // Create an HttpEntity with the request body and headers
            HttpEntity<CreateOrderRequest> requestEntity = new HttpEntity<>(requestObject, headers);
            // Send the POST request and receive the response
            return restTemplate.postForObject(
                    baseUrl + "?type={type}&chat_id={chatId}&orderItem={orderItemList}&tin={tin}",
                    requestEntity,
                    OrderDocument.class,
                    Map.of(
                            "type", "post_document",
                            "chatId", chatId,
                            "orderItemList", orderItemString,
                            "tin", userTIN
                    )
            );
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
//            throw new ApiNotWorkingException(e.getMessage(), null);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiNotWorkingException(
                    "Document sozdat qilishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "post_document",
                                    "chatId", chatId,
                                    "orderItem", orderItemString
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    private String orderItemListToJsonStringMapper(List<OrderItem> orderItemList) {
        StringBuilder result = new StringBuilder("[");
        for (OrderItem orderItem : orderItemList) {
            result.append(String.format("{    " +
                    "\"itemID\":\"%s\"," +
                    "\"Quantity\":%s," +
                    "\"Price\":%s" +
                    "}", orderItem.itemID(), orderItem.Quantity(), orderItem.Price()
            ));
        }
        result.append("]");
        return result.toString();
    }


    public ContrAgentList getContrAgentList(User user, int currentPage, @Nullable String contrAgentName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
            headers.set("Content-Type", "application/json");
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("type", "contragent")
                    .queryParam("chat_id", user.getTelegramId())
                    .queryParam("page", currentPage)
                    .queryParam("page_size", AppConstants.PAGE_SIZE);

            if (!Objects.isNull(contrAgentName)) {
                builder.queryParam("name", contrAgentName);
            }

            ResponseEntity<ContrAgentList> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    ContrAgentList.class
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "ContrAgent larni olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "contragent",
                                    "chatId", user.getTelegramId()
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }

    public UserTIN searchContrAgentTIN(Long chatId, String searchKey) {
        try {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, this.apiKey);
        headers.set("Content-Type", "application/json");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("type", "contragent")
                .queryParam("chat_id", chatId)
                .queryParam("tin", searchKey);

        ResponseEntity<UserTIN> responseEntity = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserTIN.class
        );
        return responseEntity.getBody();
        } catch (Exception e) {
            throw new ApiNotWorkingException(
                    "Contragent ni olishda nimadir xato ketdi",
                    Map.of(
                            "methodParams", Map.of(
                                    "request-type", "contragent",
                                    "chatId", chatId
                            ),
                            "exception", e.getMessage()
                    )
            );
        }
    }
}
