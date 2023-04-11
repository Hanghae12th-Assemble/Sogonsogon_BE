//package com.sparta.sogonsogon.noti.util;
//
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DataSourceConfig {
//
//    @Value("${spring.datasource.url}")
//    private String url;
//
//    @Value("${spring.datasource.username}")
//    private String username;
//
//    @Value("${spring.datasource.password}")
//    private String password;
//
//    @Value("${spring.datasource.driver-class-name}")
//    private String driverClassName;
//
//    @Bean
//    public DataSource dataSource() {
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setJdbcUrl(url);
//        dataSource.setUsername(username);
//        dataSource.setPassword(password);
//        dataSource.setDriverClassName(driverClassName);
//
//        // 커넥션 풀 설정
//        /* 데이터베이스 연결 풀에서 유지할 최소한의 유휴 커넥션 개수를 설정하는 코드 */
//        dataSource.setMinimumIdle(5);
//        /* 커넥션 풀의 최대 크기를 61로 설정*/
//        dataSource.setMaximumPoolSize(61);
//        /* 커넥션 풀에 대기중인 커넥션 중에서 얼마나 오랫동안 대기하고 있으면 해당 커넥션을 폐기할지를 결정하는 설정*/
//        dataSource.setIdleTimeout(30000);
//        /* 이 코드는 커넥션의 최대 생존 시간을 설정하는 것입니다.
//        설정된 값은 밀리초 단위이며, 이 시간이 지나면 커넥션이 소멸됩니다.  */
//        dataSource.setMaxLifetime(1800000); //30분
//        /* 커넥션을 가져오기 위한 대기 시간을 설정하는 코드, 10초*/
//        dataSource.setConnectionTimeout(10000);
//
//        return dataSource;
//    }
//
//    @Bean
//    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
//}
//
//
