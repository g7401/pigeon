<template>
  <a-table
    v-bind="{...$attrs, ...$props}"
    v-on="$listeners"
    :scopedSlots="$scopedSlots"
  >
    <template
      :slot="column.key"
      v-for="column in $attrs.columns"
    >
      <slot :name="column.key">
      </slot>
    </template>
    <template slot="expandedRowRender" slot-scope="scope">
      <slot name="expandedRowRender" :data="scope">
      </slot>
    </template>
  </a-table>
</template>

<script>
import EditCell from './EditCell'
export default {
  components: {
    EditCell
  }
}
</script>

<style>
</style>
