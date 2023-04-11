package com.sparta.sogonsogon.noti.repository;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


//동시성을 고려하여 ConcurrentHashMap을 이용해 구현해주고 이를 저장하고 꺼내는 식의 방식을 진행한다.
//Emitter와 이벤트를 찾는 부분에 있어 startsWith을 사용하는 이유는 현재 저장하는데 있어
// 뒤에 구분자로 회원의 ID를 사용하기 때문에 해당 회원과 관련된 Emitter와 이벤트들을 찾아오는 것이다.
@Repository
@Slf4j
@NoArgsConstructor
public class EmitterRepositoryImpl implements EmitterRepository {
    //DB에 저장하지 않고, Map에 저장하고 꺼내는 방식
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    //save - Emitter를 저장한다. Emitter = 이벤트를 생성하는 메소드
    @Override
    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);
        for(Map.Entry<String, SseEmitter> entrySet : emitters.entrySet()){
            log.info(entrySet.getKey() + " : " + entrySet.getValue());
        }
        return sseEmitter;
    }

    //saveEventCache - 이벤트를 저장한다. Cache = 첫 요청 시 특정 위치에 복사본을 저장하는 것
    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        eventCache.put(eventCacheId, event);
    }

    //회원과 관련된 모든 Emitter를 찾아온다.
    @Override
    public Map<String, SseEmitter> findAllEmitterStartWithByMemberId(String memberId) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(memberId))
                .peek(entry -> System.out.printf("Emitter 값 = %s%n", entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    //회원과 관련된 모든 이벤트를 찾는다.
    @Override
    public Map<String, Object> findAllEventCacheStartWithByMemberId(String memberId) {
        return eventCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(memberId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    //Emitter를 삭제한다
    @Override
    public void deleteById(String id) {
        log.info(id + " 삭제");
        emitters.remove(id);
    }

    //회원과 관련된 모든 Emitter를 지운다
    @Override
    public void deleteAllEmitterStartWithId(String memberId) {
        emitters.forEach(
                (key, emitter) -> {
                    if (Arrays.stream(key.split("_")).findFirst().get().equals(memberId)) {
                        emitters.remove(key);
                    }
                }
        );
    }

    //회원과 관련된 모든 이벤트를 지운다.
    @Override
    public void deleteAllEventCacheStartWithId(String memberId) {
        eventCache.forEach(
                (key, emitter) -> {
                    if (key.startsWith(memberId)) {
                        log.info("deleteAll " + memberId + " 삭제");
                        eventCache.remove(key);
                    }
                }
        );
    }

}
