package com.leorces.persistence.postgres;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.HistoryPersistence;
import com.leorces.persistence.postgres.mapper.HistoryMapper;
import com.leorces.persistence.postgres.repository.ActivityRepository;
import com.leorces.persistence.postgres.repository.HistoryRepository;
import com.leorces.persistence.postgres.repository.ProcessRepository;
import com.leorces.persistence.postgres.repository.VariableRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
public class HistoryPersistenceImpl implements HistoryPersistence {

    private final HistoryRepository historyRepository;
    private final ProcessRepository processRepository;
    private final ActivityRepository activityRepository;
    private final VariableRepository variableRepository;
    private final HistoryMapper historyMapper;

    @Override
    @Transactional
    public void save(List<ProcessExecution> processes) {
        var processIds = extractProcessIds(processes);
        var activityIds = extractActivityIds(processes);
        var variableIds = extractVariableIds(processes);

        historyRepository.saveAll(historyMapper.toEntities(processes));
        processRepository.deleteAllById(processIds);
        activityRepository.deleteAllById(activityIds);
        variableRepository.deleteAllById(variableIds);
    }

    @Override
    public PageableData<ProcessExecution> findAll(Pageable pageable) {
        var pageableResult = historyRepository.findAll(pageable);
        return new PageableData<>(historyMapper.toExecutions(pageableResult.data()), pageableResult.total());
    }

    private List<String> extractProcessIds(List<ProcessExecution> processes) {
        return processes.stream()
                .map(ProcessExecution::id)
                .toList();
    }

    private List<String> extractActivityIds(List<ProcessExecution> processes) {
        return processes.stream()
                .map(ProcessExecution::activities)
                .flatMap(List::stream)
                .map(Activity::id)
                .toList();
    }

    private List<String> extractVariableIds(List<ProcessExecution> processes) {
        return Stream.concat(
                        processes.stream()
                                .flatMap(process -> process.variables().stream())
                                .map(Variable::id),
                        processes.stream()
                                .flatMap(process -> process.activities().stream())
                                .flatMap(activity -> activity.variables().stream())
                                .map(Variable::id)
                )
                .distinct()
                .toList();
    }

}
