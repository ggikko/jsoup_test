package me.ggikko;

import lombok.Builder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by Park Ji Hong, ggikko.
 */

@Builder
public class MainClass {

    private static String Accept = "Accept";
    private static String Accept_Value = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private static String url = "http://cafeblog.search.naver.com/search.naver?sm=tab_hty.top&where=post&ie=utf8&query=%EC%84%A0%EB%A6%89+%ED%9A%8C%EC%82%AC+6000%EC%9B%90";
    private static String url2 = "http://cafeblog.search.naver.com/search.naver?where=post&sm=tab_pge&query=%EC%84%A0%EB%A6%89+%ED%9A%8C%EC%82%AC+%EC%A0%90%EC%8B%AC&st=sim&date_option=0&date_from=&date_to=&dup_remove=1&post_blogurl=&post_blogurl_without=&srchby=all&nso=&ie=utf8&start=1";
    private static String url3 = "https://search.naver.com/search.naver?where=post&sm=tab_pge&query=%EC%84%A0%EB%A6%89+%ED%9A%8C%EC%82%AC+%EC%A0%90%EC%8B%AC&st=sim&date_option=0&date_from=&date_to=&dup_remove=1&post_blogurl=&post_blogurl_without=&srchby=all&nso=&ie=utf8&start=1";

    public static void main(String[] args) throws IOException, URISyntaxException {

        long start = System.currentTimeMillis();

        //TODO : 사이즈를 정해놓는게 조금 더 빠르다.
        List<String> test = new ArrayList<>();

        MainClass mainClass = new MainClass();
        List<String> famousRestaurantNameFromCsvFile = mainClass.getFamousRestaurantNameFromCsvFile();

        for (int i = 0; i < 4; i++) {
            Elements elements = getDataFromSpecificUrl(i);
            List<String> tempList = new ArrayList<String>();
            for (Element temp : elements) {
                tempList = splitWords(temp.text());
                tempList = preprocessing(tempList);
                List<String> test2 = csvFileCompareWithBlogInfo(famousRestaurantNameFromCsvFile, tempList);
                printStrings(test2);
            }
        }

//        Optional.ofNullable("stringvalue").



        long end = System.currentTimeMillis();
        System.out.printf(String.valueOf(end - start));
    }

    //블로그에서 가져온 리스트가 음식점 csv파일에 존재하는지 검사하고 있으면 이를 List로 반환한다
    public static List<String> csvFileCompareWithBlogInfo(List<String> csvFileList, List<String> blogDataList){
        List<String> real = new ArrayList<>();

        for(int i=0; i < blogDataList.size(); i++){

            for(int j=0; j< csvFileList.size(); j++){
                String blogData = blogDataList.get(i);
                String csvData = csvFileList.get(j);

                if(csvData.contains(blogData)){
                    real.add(csvData);
                }
            }
        }

        return real;
    }

    //음식점 주소와 정보가 들어가 있는 csv파일 정보를 읽어들여 음식점 정보를 메모리 상에 올려 놓는다
    public List<String> getFamousRestaurantNameFromCsvFile() throws IOException {

        //CSV파일에 맵핑될 Header를 정의해준다.
        final String name = "업소명";
        final String kind = "업종";
        final String addr = "지번";
        final String addrs = "도로명";

        //File Reader
        FileReader fileReader = null;

        //CSV Parser
        CSVParser csvParser = null;

        //CSV파일에 맵핑될 Header를 정의해준다.
        final String[] FILE_HEADER_MAPPING = {"연번", "업소명", "업종", "지번", "도로명"};

        //CSVFormat
        CSVFormat csvFormat = CSVFormat.newFormat(',').withQuote('"').withHeader(FILE_HEADER_MAPPING);

        //CSV파일을 읽어온다
        fileReader = new FileReader(new File(getClass().getClassLoader().getResource("restaurant.csv").getFile()));

        //CSV파일 파서에 파일 헤더 맵핑과 구분자 포맷을 설정해준다
        csvParser = new CSVParser(fileReader, csvFormat);

        //전체 음식점 정보 이름 리스트
        List<String> entireRestaurantName = new ArrayList<>();

        //모든 줄의 레코드를 리스트에 담는다
        for (CSVRecord record : csvParser) {
            String tempRestaurantName = record.get("업소명").trim().replaceAll(" ","").replaceAll("\\s","");
            entireRestaurantName.add(tempRestaurantName);

        }

        //해당 리스트 반환
        return entireRestaurantName;
    }


    //키워드로 검색된 네이버 블로그에서 정보 가져옴
    public static Elements getDataFromSpecificUrl(int i) throws IOException {
        Document document = Jsoup.connect(url2 + String.valueOf(i))
                .header(Accept, Accept_Value)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .get();
        return document.select("a.sh_blog_title");
    }

    //리스트 데이터 프린트
    public static void printStrings(List<String> list) {
        for (int j = 0; j < list.size(); j++) {
            System.out.printf(list.get(j) + "\n");
        }
    }

    //단어들 쪼개기
    public static List<String> splitWords(String sentence) {
        StringTokenizer st = new StringTokenizer(sentence, " ");
        List<String> tempList = new ArrayList<>();
        while (st.hasMoreTokens()) {
            tempList.add(st.nextToken());
        }
        return tempList;
    }


    //단어 전처리 과정 필요없는 문구 단어 삭제
    public static List<String> preprocessing(List<String> inputWords) {
        List<String> collect = inputWords
                .parallelStream()
                .map(s -> s.replaceAll("[-+.^:,/<>~()!]", ""))
                .map(s -> s.replaceAll("\\[", ""))
                .map(s -> s.replaceAll("\\]", ""))
                .map(s -> s.replaceAll("★", ""))
                .map(s -> s.trim())
                .filter(s -> !(s.length() == 1))
                .filter(s -> !s.contains("선릉"))
                .filter(s -> !s.isEmpty()) //자바 1.6 이후부터
                .filter(s -> !s.contains("점심"))
                .filter(s -> !s.contains("맛있"))
                .filter(s -> !s.contains("먹을"))
                .filter(s -> !s.contains("만한"))
                .filter(s -> !s.contains("핵맛"))
                .filter(s -> !s.contains("실패"))
                .filter(s -> !s.contains("느낄"))
                .filter(s -> !s.contains("있는"))
                .filter(s -> !s.contains("맛집"))
                .filter(s -> !s.contains("먹고"))
                .filter(s -> !s.contains("밥집"))
                .filter(s -> !s.contains("왔어요"))
                .filter(s -> !s.contains("먹다"))
                .filter(s -> !s.contains("한식"))
                .collect(toList());
        return collect;
    }
}

//
//
//
//    //restaouratnt.json 파일 정보 메모리로 가져오기 ex: 서울 특별시 강남구만 데이터 가져오기 메모리로
//    public String getJsonData() throws URISyntaxException, IOException {
//
//        StringBuilder result = new StringBuilder("");
//
//        //Get file from resources folder
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(classLoader.getResource("restaurant.txt").getFile());
//
//        try (Scanner scanner = new Scanner(file)) {
//
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                result.append(line);
//            }
//
//            scanner.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return result.toString();
//
//    }
//

//
//    //파일리더
//    public void readJsonFile() throws IOException {
//
//        final String number = "연번";
//        final String name = "업소명";
//        final String kind = "업종";
//        final String addr = "지번";
//        final String addrs = "도로명";
//
//
//        final String[] FILE_HEADER_MAPPING = {"연번", "업소명", "업종", "지번", "도로명"};
//
//        FileReader fileReader = null;
//        CSVParser csvParser = null;
//        CSVFormat csvFormat = CSVFormat.newFormat(',').withQuote('"').withHeader(FILE_HEADER_MAPPING);
////                .DEFAULT.withHeader(FILE_HEADER_MAPPING);
//
//        fileReader = new FileReader(new File(getClass().getClassLoader().getResource("restaurant.csv").getFile()));
//        csvParser = new CSVParser(fileReader, csvFormat);
//
//        List<Restaurant> list = new ArrayList();
//        List<String> restaurantList = new ArrayList<>();
//
//        for (CSVRecord record : csvParser) {
//            restaurantList.add(record.get("업소명") + "\n");
//        }
//
//        for (int i = 0; i < restaurantList.size(); i++) {
//            System.out.printf(restaurantList.get(i));
//        }
//    }

// 객체로 가져오기
//       List<CSVRecord> records = csvParser.getRecords();
//
//        for(int i=1; i<100; i++){
//            CSVRecord record = records.get(i);
//            Restaurant restaurant = new Restaurant(record.get(number), record.get(name), record.get(kind), record.get(addr), record.get(addrs));
//            list.add(restaurant);
//        }
//
//        for(Restaurant restaurant : list){
//            System.out.printf(restaurant.toString() + "\n");
//        }


//        BufferedReader br = new BufferedReader(new FileReader(new File(getClass().getClassLoader().getResource("restaurant.csv").getFile())));
//        StringBuilder sb = new StringBuilder();
//        String line = br.readLine();
//        while(line !=null){
//            sb.append(line);
//            line = br.readLine();
//        }
//        String result = "";
//        result = sb.toString();
//        return result;
//        JSONParser parser = new JSONParser();
//        JSONObject jsonObject = parser.parse()
//    }//파일리더
//    public void readJsonFile() throws IOException {
//
//        final String number = "연번";
//        final String name = "업소명";
//        final String kind = "업종";
//        final String addr = "지번";
//        final String addrs = "도로명";
//
//
//        final String [] FILE_HEADER_MAPPING = {"연번","업소명","업종","지번","도로명"};
//
//        FileReader fileReader = null;
//        CSVParser csvParser = null;
//        CSVFormat csvFormat = CSVFormat.newFormat(',').withQuote('"').withHeader(FILE_HEADER_MAPPING);
////                .DEFAULT.withHeader(FILE_HEADER_MAPPING);
//
//        fileReader = new FileReader(new File(getClass().getClassLoader().getResource("restaurant.csv").getFile()));
//        csvParser = new CSVParser(fileReader, csvFormat);
//
//        List<Restaurant> list = new ArrayList();
//        List<String> restaurantList = new ArrayList<>();
//
//        for(CSVRecord record : csvParser){
//            restaurantList.add(record.get("업소명")+"\n");
//        }
//
//        for(int i=0; i<restaurantList.size(); i++){
//            System.out.printf(restaurantList.get(i));
//        }


// 객체로 가져오기
//       List<CSVRecord> records = csvParser.getRecords();
//
//        for(int i=1; i<100; i++){
//            CSVRecord record = records.get(i);
//            Restaurant restaurant = new Restaurant(record.get(number), record.get(name), record.get(kind), record.get(addr), record.get(addrs));
//            list.add(restaurant);
//        }
//
//        for(Restaurant restaurant : list){
//            System.out.printf(restaurant.toString() + "\n");
//        }


//        BufferedReader br = new BufferedReader(new FileReader(new File(getClass().getClassLoader().getResource("restaurant.csv").getFile())));
//        StringBuilder sb = new StringBuilder();
//        String line = br.readLine();
//        while(line !=null){
//            sb.append(line);
//            line = br.readLine();
//        }
//        String result = "";
//        result = sb.toString();
//        return result;
//        JSONParser parser = new JSONParser();
//        JSONObject jsonObject = parser.parse()
//    }

//            for (Element temp : elements) {
//
//                tempList = splitWords(temp.text());
//                tempList = preprocessing(tempList);
//                printStrings(tempList);

//                StringTokenizer st = new StringTokenizer(temp.text(), " ");
//                while (st.hasMoreTokens()) {
//                    tempList.add(st.nextToken());
//                }

//                System.out.printf(temp.text() + "\n");
//                Character.isLetterOrDigit(temp.text().charAt());

//            List<String> collect = tempList
//                    .stream()
//                    .parallel()
//                    .map(s -> s.replaceAll("[-+.^:,/<>~()!]", ""))
//                    .map(s -> s.replaceAll("\\[", ""))
//                    .map(s -> s.replaceAll("\\]", ""))
//                    .map(s -> s.replaceAll("★", ""))
//                    .map(s -> s.trim())
//                    .filter(s -> !(s.length() == 1))
//                    .filter(s -> !s.contains("선릉"))
//                    .filter(s -> !s.equals(""))
//                    .filter(s -> !s.contains("점심"))
//                    .filter(s -> !s.contains("맛있"))
//                    .filter(s -> !s.contains("먹을"))
//                    .filter(s -> !s.contains("만한"))
//                    .filter(s -> !s.contains("핵맛"))
//                    .filter(s -> !s.contains("실패"))
//                    .filter(s -> !s.contains("느낄"))
//                    .filter(s -> !s.contains("있는"))
//                    .filter(s -> !s.contains("맛집"))
//                    .filter(s -> !s.contains("먹고"))
//                    .filter(s -> !s.contains("밥집"))
//                    .filter(s -> !s.contains("왔어요"))
//                    .filter(s -> !s.contains("먹다"))
//                    .filter(s -> s.contains("교동전선생"))
//                    .collect(toList());


//        List<String> title = elements.stream().parallel().map(t -> t.attr("title")).collect(toList());
//        System.out.printf(title.get(0));
//            System.out.printf(String.valueOf(i));

//Sysstem.out.printf(String.valueOf(System.currentTimeMillis() - start) + "\n");


// a인프라 -> b+1 키워드 b알고리즘 -> c다비 -> d클라이언

