/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.emails.dao.stubs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 * FIXME : Remove this class !!! and use Spring instead
 */
public class JpaRepositoryStub<T extends IIdentifiable<Long>> extends RepositoryStub<T>
        implements JpaRepository<T, Long> {

    @Override
    public List<T> findAll() {
        return entities;
    }

    @Override
    public List<T> findAll(final Iterable<Long> pIds) {
        try (final Stream<Long> stream = StreamSupport.stream(pIds.spliterator(), false)) {
            return stream.map(id -> findOne(id)).collect(Collectors.toList());
        }
    }

    /**
     * Get entities
     *
     * @return The list of entities
     */
    @Override
    protected List<T> getEntities() {
        return entities;
    }

    /**
     * Set entities
     *
     * @param pEntities
     *            The list of entities
     */
    @Override
    protected void setEntities(final List<T> pEntities) {
        entities = pEntities;
    }

    @Override
    public Page<T> findAll(final Pageable pPageable) {
        final List<T> elements = new ArrayList<>();
        final int firstIndex = pPageable.getPageNumber() * pPageable.getPageSize();
        if ((this.entities != null) && !this.entities.isEmpty()) {
            elements.addAll(this.entities.subList(firstIndex, this.entities.size()).stream()
                                    .limit(pPageable.getPageSize()).collect(Collectors.toList()));
            return new PageImpl<>(elements, pPageable, this.entities.size());
        } else {
            return new PageImpl<>(new ArrayList<>(), pPageable, 0);
        }

    }

    @Override
    public <S extends T> S findOne(final Example<S> pExample) {
        // Not implemented yet
        return null;
    }

    @Override
    public <S extends T> Page<S> findAll(final Example<S> pExample, final Pageable pPageable) {
        // Not implemented yet
        return new PageImpl<>(new ArrayList<>());
    }

    @Override
    public <S extends T> long count(final Example<S> pExample) {
        // Not implemented yet
        return 0;
    }

    @Override
    public <S extends T> boolean exists(final Example<S> pExample) {
        // Not implemented yet
        return false;
    }

    @Override
    public List<T> findAll(final Sort pSort) {
        // Not implemented yet
        return new ArrayList<>();
    }

    @Override
    public void flush() {
        // Not implemented yet
    }

    @Override
    public <S extends T> S saveAndFlush(final S pEntity) {
        // Not implemented yet
        return null;
    }

    @Override
    public void deleteInBatch(final Iterable<T> pEntities) {
        // Not implemented yet
    }

    @Override
    public void deleteAllInBatch() {
        // Not implemented yet
    }

    @Override
    public T getOne(final Long pId) {
        // Not implemented yet
        return null;
    }

    @Override
    public <S extends T> List<S> findAll(final Example<S> pExample) {
        // Not implemented yet
        return new ArrayList<>();
    }

    @Override
    public <S extends T> List<S> findAll(final Example<S> pExample, final Sort pSort) {
        // Not implemented yet
        return new ArrayList<>();
    }

}