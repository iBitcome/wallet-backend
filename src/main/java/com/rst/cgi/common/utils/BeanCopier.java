package com.rst.cgi.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BeanCopier {
    private final Logger logger = LoggerFactory.getLogger(BeanCopier.class);

    private static BeanCopier _instance;
    private LinkedHashMap<String, org.springframework.cglib.beans.BeanCopier> copierMap;

    private BeanCopier() {
        init();
    }

    public static BeanCopier getInstance() {
        if (_instance == null) {
            _instance = new BeanCopier();
        }
        return _instance;
    }

    @SuppressWarnings({"unchecked", "serial", "rawtypes"})
    public void init() {
        this.copierMap = new LinkedHashMap(16, 0.75F, true) {

            @SuppressWarnings("unused")
            protected boolean removeEldestEntry(Map<String, org.springframework.cglib.beans.BeanCopier> eldest) {
                return size() > 128;
            }
        };
    }

    public void copyBean(Object original, Object target) {
        if (original == null) {
            return;
        }
        String fromName = original.getClass().getName();
        String toName = target.getClass().getName();
        StringBuilder key = new StringBuilder(fromName).append("->").append(toName);
        org.springframework.cglib.beans.BeanCopier copier = (org.springframework.cglib.beans.BeanCopier) this.copierMap
                .get(key.toString());
        if (copier == null) {
            copier = org.springframework.cglib.beans.BeanCopier.create(original.getClass(), target.getClass(), false);
            this.copierMap.put(key.toString(), copier);
        }
        copier.copy(original, target, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T1, T2> List<T2> copyCollection(Collection<T1> originalCollection, Class<T2> targetClass) {
        List toList = new ArrayList();
        if (originalCollection == null) {
			return toList;
        }
        for (Object from : originalCollection) {
            try {
                Object to = targetClass.newInstance();
                copyBean(from, to);
                toList.add(to);
            } catch (Exception e) {
                this.logger.error("Bean copy error occurs when from [{}] to [{}]", from.getClass().getName(),
                        from.getClass().getName());
                return null;
            }
        }
        return toList;
    }
}

